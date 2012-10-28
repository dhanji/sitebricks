/**
 * @fileoverview Client/Server RPC encapsulation. Relies on the presence
 * of jQuery 1.4.2.
 * Classes:
 *   ryter.Rpc
 */

var sitebricks = sitebricks || {};

sitebricks.HOSTNAME = document.location.host.toString();
sitebricks.BASE_URL = document.location.toString().replace('#', '');
if (sitebricks.BASE_URL.charAt(sitebricks.BASE_URL.length - 1) != '/')
  sitebricks.BASE_URL += '/';

/**
 *
 * @type {string}
 */
sitebricks.SOCKET_ID = '$_SITEBRICKS_SOCKET_ID_$';

/**
 *
 * @type {string}
 */
sitebricks.URL_PREFIX = '$_SITEBRICKS_URL_PREFIX_$';

/**
 * The delay after the last bit of activity to send a ping to the server.
 */
sitebricks.PING_RATE_MS = 25000;

/**
 * The maximum number of backoffs to register in a cubic backoff function.
 */
sitebricks.BACKOFF_CAP = 7;

/**
 * Whether or not this is the mozilla web browser.
 */
sitebricks.IS_MOZILLA = navigator.userAgent.indexOf("Firefox") != -1;

/**
 * Encapsulates client/server RPCs.
 *
 * @constructor
 */
sitebricks.Channel = function(opt_url_context) {
  opt_url_context = opt_url_context || '';

  var self = this;
  /**
   * Callback registration function.
   * @param event an event name (any of: connected, disconnected, message)
   */
  this.on = function(event, callback) {
    if (event == 'message')
      self.callback_ = callback;
    else if (event == 'connect')
      self.onReconnectCallback = callback;
    else if (event == 'disconnect')
      self.onDisconnectCallback = callback;
    else
      throw 'Unknown event type: ' + event;
  };

  this.onReconnectCallback = null;
  this.onDisconnectCallback = null;

  // Firefox validates all returned JSON, which spews all kinds of errors.
  if (sitebricks.IS_MOZILLA) {
    $.ajaxSetup({ 'beforeSend': function(xhr) {
      if (xhr.overrideMimeType)
          xhr.overrideMimeType("text/plain");
      }
    });
  }

  /**
   * Class that handles RPC transport via HTML5 Websocket.
   */
  var WebSocketTransport = {
    url: '',
    handlers_: {},
    /**
     * @export
     */
    connect: function() {
      var self = this;
      var url = (document.location.protocol.toString() + "//" + sitebricks.HOSTNAME)
        .replace('http:', 'ws:')
        .replace('https:', 'wss:')
        .replace('#', '');
      if (url.charAt(url.length - 1) == '/')
        url = url.slice(0, url.length - 1);
      url += opt_url_context
        + sitebricks.URL_PREFIX
        + '/websocket?SBSocketId=' + sitebricks.SOCKET_ID;
      self.url = url;
      this.ws_ = typeof MozWebSocket == 'undefined' ? new WebSocket(url) : new MozWebSocket(url);
      this.ws_.onopen = this.handlers_.connect || function() {};
      this.ws_.onclose = this.handlers_.disconnect || function() {};
      this.ws_.onerror = this.handlers_.error || function() {};
      this.ws_.onmessage = function(message) { self.handlers_.message(message.data); };
      console.log(String(new Date()), "Attempting socket connect.");
    },

    /**
     * @export
     */
    send: function(message) {
      this.ws_.send(message);
    },

    /**
     * @export
     */
    on: function(event, handler) {
      this.handlers_[event] = handler;
    },

    /**
     * @export
     */
    isOpen: function() {
      return this.ws_.readyState == 1 /* CONNECTED */;
    },

    /**
     * @export
     */
    disconnect: function() {
      this.ws_.close();
    }
  };

  /**
   * Class that handles RPC transport via XHR Long-polling.
   */
  var CometTransport = {
    handlers_: {},
    channels_: 0,
    url: '',
    first: true,

    /**
     * @export
     */
    connect: function() {
      var self = this;
      var url = sitebricks.BASE_URL;
      url += sitebricks.URL_PREFIX + '/async?SBSocketId=' + sitebricks.SOCKET_ID;

      self.url = url;
      self.channels_++;
      $.ajax({
        type: 'GET',
        url: self.url,
        dataType: 'text',
        success: function(data) {
          self.handlers_.message(data);

          self.channels_--;
          if (self.channels_ <= 1)
            self.connect();
        },
        failure: function() {
          self.channels_--;
          if (self.handlers_.error)
            self.handlers_.error()
        }
      });

      // We have to simulate a connected event.
      if (self.first) {
        self.first = false;
        self.handlers_.connect();
      }
    },

    /**
     * @export
     */
    send: function(message) {
      var self = this;
      $.ajax({
        type: 'POST',
        url: self.url,
        processData: false,
        contentType: 'application/json',
        data: message,
        success: function(data) {
          /* Ignore response. */
        }
      });
    },

    /**
     * @export
     */
    on: function(event, handler) {
      this.handlers_[event] = handler;
    },

    /**
     * @export
     */
    isOpen: function() {
      return this.channels_ > 0;
    },

    /**
     * @export
     */
    disconnect: function() {}
  };

  if (window.WebSocket || window.MozWebSocket)
    this.transport_ = WebSocketTransport;
  else
    this.transport_ = CometTransport;

  this.transport_.reconnectAttempts_ = 1;
  this.transport_.on('message', function(data) {
    self.onMessage_(data);
  });
  this.transport_.on('connect', function() {
    console.log(String(new Date()), "Socket connected.");
    if (self.transport_.reconnectAttempts_ && self.onReconnectCallback) {
      // If this is not the first time connection, refresh ourselves.
      self.onReconnectCallback();
    }
    self.transport_.reconnectAttempts_ = 0;
    self.sync();
  });
  this.transport_.on('disconnect', function() {
    if (self.transport_.reconnectAttempts_ <= 8)
      self.transport_.reconnectAttempts_++;

    // If we have been disconnected for awhile, fire the disconnected alert.
    if (self.transport_.reconnectAttempts_ > 3 && self.onDisconnectCallback)
      self.onDisconnectCallback();

    // Reconnect after backoff.
    var delay = Math.pow(self.transport_.reconnectAttempts_, 3) * 100;
    if (isNaN(delay))
      delay = 1001;
    console.log(String(new Date()), "Socket closed, reconnecting in", delay, "ms");
    setTimeout(function() {
      self.transport_.connect();
    }, delay);
  });
  this.transport_.on('error', function(data) {
    console.log('ERROR', data)
  });
  this.transport_.forceReconnect = function() {
    try {
      self.transport_.disconnect();
    } finally {
      // Fire disconnect listener if it did not happen gracefully.
      if (self.transport_.isOpen()) {
        self.transport_.handlers_['disconnect']();
      }
    }
  };

  // Use offline detection to supplement the normal lifecycle of a transport.
  // This is by no means fool-proof, if you can reach an intranet but not the
  // sitebricks server, then it's useless. But it will serve for wake-from-sleep.
  // See: https://developer.mozilla.org/en/Online_and_offline_events
  $(window).bind('offline', function() {
    console.log(String(new Date()), 'Offline event received.');
    self.transport_.forceReconnect();
  });
  $(window).bind('online', function() {
    if (self.transport_.reconnectAttempts_ > 1 || sitebricks.Channel.isPingLate_()) {
      console.log(String(new Date()), 'Online event received. Accelerating reconnect...');
      self.transport_.reconnectAttempts_ = 1;
      self.transport_.forceReconnect();
    }
  });
};

/**
 * Returns true if the last ping time was longer than N seconds ago, signaling a
 * failed connection to the server.
 */
sitebricks.Channel.isPingLate_ = function() {
  return Math.ceil(new Date().getTime() - sitebricks.lastPizzing_) > 45000; /* millis */
};

/**
 * On message callback.
 *
 * @type {function()}
 */
sitebricks.Channel.prototype.callback_ = null;

/**
 * @export
 *
 * Underlying RPC send function using pluggable transport.
 */
sitebricks.Channel.prototype.send = function(opt_event, data) {
  // Only send if open, otherwise queue up the messages to send on reconnect.
  if (this.transport_.isOpen()) {
    if (opt_event)
      data = opt_event + ':' + data;
    this.transport_.send(data);
  }
};

/**
 * Underlying RPC dispatch/push-receiver function using Websocket.
 */
sitebricks.Channel.prototype.onMessage_ = function(data) {
  // Mark the timestamp of the last bit of incoming communication.
  sitebricks.lastPizzing_ = new Date().getTime();

  if (this.callback_)
    this.callback_.call(this, data);
  else
    console.log("No callback registered. Sign one up with Channel.on('message', function(data) { .. })", data);

  // Ping every 25 seconds of idleness. We push the timer back if necessary.
  if (!sitebricks.pizzing_) {
    sitebricks.pizzing_ = setTimeout(sitebricks.Channel.ping_, sitebricks.PING_RATE_MS);
  } else {
    // Reset the ping timer since we have had some activity.
    clearTimeout(sitebricks.pizzing_);
    sitebricks.pizzing_ = setTimeout(sitebricks.Channel.ping_, sitebricks.PING_RATE_MS);
  }
};

/**
 * Periodic ping function that keeps the websocket from disconnecting.
 */
sitebricks.Channel.ping_ = function() {
  // Looks like we're connecting after a major delay.
  if (sitebricks.Channel.isPingLate_()) {
    sitebricks.Channel.transport_.disconnect();
  } else
    sitebricks.Channel.transport_.send('ping', '');  // Only sync if we're connected!

  // Schedule the next ping.
  sitebricks.pizzing_ = setTimeout(sitebricks.Channel.ping_, sitebricks.PING_RATE_MS);
};

sitebricks.Channel.prototype.sync = function() {
  this.transport_.send('ping', '');
};

/**
 * Starts the underlying transport and connect to the server.
 */
sitebricks.Channel.prototype.connect = function() {
  this.transport_.connect();
};

