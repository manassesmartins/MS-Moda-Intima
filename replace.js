const fs = require('fs');

let html = fs.readFileSync('web/index.html', 'utf8');

// The marker where auth-panel starts
const startAuth = '<div id="auth-panel"';
// The marker where the script Google Auth handlers begin
const endAuth = '<!-- END GOOGLE GOOGLE MODAL -->'; // Wait let's just find the closing tag of auth-panel. Wait, the auth panel contains the Google picker modal.
