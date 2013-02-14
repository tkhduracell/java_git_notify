git-notify
==========

GitWeb Rss Watcher

A small tray-utility for polling commits on a remote git server using 'gitweb' web interface and its RSS feature. 



Settings are stored in a jsonfile named git-notify.json.

Requried values:
"port":80
Target port

"project":"myproject.git"
Including .git

"intervall":120000
milliseconds between polling
	
"host":"myhost.com"
host without http:// ect. , /gitweb will automatically be added
	
"user":"myuser"
"pass":"mypass"
