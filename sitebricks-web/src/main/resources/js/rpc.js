/**
 * @fileoverview Client/Server RPC encapsulation. Relies on the presence
 * of jQuery 1.4.3.
 * Classes:
 *   sitebricks.Rpc
 */

var sitebricks = sitebricks || {};


/**
 * Encapsulates client/server RPCs.
 *
 * @constructor
 */
sitebricks.Rpc = function() {
};

/**
 * Underlying RPC dispatch function using Ajax. Uses Json batching.
 *
 * @export
 * @param args Rpc request data.
 */
sitebricks.Rpc.prototype.rpc = function(args, opt_callback) {
  var url = '/ajax/' + args.rpc;
  var self = this;
  args = { data: JSON.stringify(args) };
  opt_callback = opt_callback || function() {};

  $.ajax({
    type: 'POST',
    url: url,
    dataType: 'json',
    data: args,
    success: opt_callback,
    failure: function() {
      alert("Failed to contact server =(");
    }
  });
};
