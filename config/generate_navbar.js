// Generates HTML code to for a navigation bar offering navigation to pages that aren't the argument.
function generateNavbar(current_page_id, depth) {
  var pages = [
    ['id': 'archives', 'url': String("../").repeat(depth) + 'archives.html'}
  ];
  // var ret = "<table class='navtable'><tr>"
  // <table class='navtable'><tr>
  // <td>
  return pages[0].url;
}
