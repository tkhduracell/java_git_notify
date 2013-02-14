git-notify
==========

GitWeb Rss Watcher

Settings are stored in a jsonfile named git-notify.json.

{
  "port":80,
	"project":"myproject.git",  //including .git
	"intervall":120000,         //milliseconds between polling
	"host":"myhost.com",        //host without http:// ect.
                              // /gitweb will automatically be added
	"user":"myuser",
	"pass":"mypass"
}
