/**
 * Sitebricks.info Website.
 */
$(function() {
  var rpc = new sitebricks.Rpc();

  // Open a new document in place.
  $('a.wikilink').live('click', function() {
    var name = $(this).attr('name');
    rpc.rpc({ rpc: 'page/' + name }, function(data) {
      $('#main article').html(data);
    });
  });


  // Edit/delete controls.
  $('#edit').click(function() {
    var name = $('#main article').attr('name');
    $('nav#controls .view').hide();
    $('nav#controls .edit').show();
    $('#editor').fadeIn();
    $('#editor article').text('');

    rpc.rpc({ rpc: 'markdown/' + name }, function(data) {
      var article = $('#editor article');
      article.text(data);
      article.focus();
    });
  });

  // Save/Discard controls.
  $('#save').click(function() {
    $('#editor').fadeOut('fast');
    $('nav#controls .edit').hide();
    $('nav#controls .view').show();

    var name = $('#main article').attr('name');
    rpc.rpc({ rpc: 'save/' + name, text: $('#editor article').text() });
  });

  $('#discard').click(function() {
    $('#editor').fadeOut('fast');
    $('nav#controls .edit').hide();
    $('nav#controls .view').show();
  });
});
