package org.danann.cernunnos.runtime.web;

import org.junit.Assert;
import org.junit.Test;

class SettingsTest {

    @Test
    void testLocateContextConfig_notSpecified() {

        def webappRootContext = 'http:/foo/bar/';
        def userSpecifiedContextLocation = null;
        def defaultUrl = new URL('http://www.yahoo.com');

        def expectedLocation = new URL('http://www.yahoo.com');
        def actualLocation = Settings.locateContextConfig(webappRootContext, 
                                    userSpecifiedContextLocation, 
                                    defaultUrl);

        Assert.assertEquals(expectedLocation.toExternalForm(), actualLocation.toExternalForm());

    }
    
    @Test
    void testLocateContextConfig_webappLocationSpecified() {

        def webappRootContext = 'http:/foo/bar/';
        def userSpecifiedContextLocation = '/WEB-INF/monkey.xml';
        def defaultUrl = new URL('http://www.yahoo.com');

        def expectedLocation = new URL('http:/foo/bar/WEB-INF/monkey.xml');
        def actualLocation = Settings.locateContextConfig(webappRootContext, 
                                    userSpecifiedContextLocation, 
                                    defaultUrl);

        Assert.assertEquals(expectedLocation.toExternalForm(), actualLocation.toExternalForm());

    }

    @Test
    void testLocateContextConfig_nonWebappLocationSpecified() {

        def webappRootContext = 'http:/foo/bar/';
        def userSpecifiedContextLocation = 'ftp:/foo/bar/monkey.xml';
        def defaultUrl = new URL('http://www.yahoo.com');

        def expectedLocation = new URL('ftp:/foo/bar/monkey.xml');
        def actualLocation = Settings.locateContextConfig(webappRootContext, 
                                    userSpecifiedContextLocation, 
                                    defaultUrl);

        Assert.assertEquals(expectedLocation.toExternalForm(), actualLocation.toExternalForm());

    }

}
