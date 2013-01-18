picasa-play
===========

[Picasa Web Albums](http://picasaweb.google.com/ "Picasa Web Albums") multi-account frontend powered by [Play Framework](http://playframework.org/ "Play Framework")

Installation
---
1. Unzip ``picasa-play-1.0-SNAPSHOT.zip`` into current directory

2. ``cd picasa-play-1.0-SNAPSHOT``

3. Create ``config.xml`` file like below:
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
4. Start server using ``sh start`` (on Windows ``java -cp "./lib/*;" play.core.server.NettyServer .``)

