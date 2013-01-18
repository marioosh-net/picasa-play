picasa-play
===========

[Picasa Web Albums](http://picasaweb.google.com/ "Picasa Web Albums") multi-account frontend powered by [Play Framework](http://playframework.org/ "Play Framework")

Build
---
To build You need [Play Framework](http://playframework.org/ "Play Framework") installation (working ``play`` command).

Clone repository

    git clone git://github.com/marioosh-net/picasa-play.git

Change directory to project path and build the standalone application package

    cd picasa-play
    play dist
    
Package is created in `dist/picasa-play-1.0-SNAPSHOT.zip`
    
Run
---    
Unzip application package

    cd dist
    unzip picasa-play-1.0-SNAPSHOT.zip
    cd picasa-play-1.0-SNAPSHOT

Create ``config.xml`` file like below. You can ommit this step - will be used default config.
```
    <configuration>
        <!-- picasa accounts -->
        <picasa>
            <account username="login1" password="password1"/>
            <account username="login2" password="password2"/>
        </picasa>
        <settings>
            <!-- local/application accounts -->
            <local>
                <account login="admin" password="password1" role="admin"/>
                <account login="user" password="password2" role="user"/>
            </local>
        </settings>
    </configuration>
```
Start server

    sh start

on Windows 

    java -cp "./lib/*;" play.core.server.NettyServer .
    

