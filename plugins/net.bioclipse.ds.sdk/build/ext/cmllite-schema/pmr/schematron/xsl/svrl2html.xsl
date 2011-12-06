<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:cml="http://www.xml-cml.org/schema" xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
  xmlns="http://www.w3.org/1999/xhtml" xmlns:dc="http://purl.org/dc/elements/1.1/" version="2.0">

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>


  <xsl:template match="svrl:schematron-output">
    <xsl:text disable-output-escaping="yes">
    <![CDATA[<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">    
    ]]>
    </xsl:text>
    <html>
      <head>
        <title>schematron failed unit tests</title>
        <style type="text/css">
          <xsl:text disable-output-escaping="yes">
/*------------------   Page structure styles   ------------------*/

* {
    margin: 0;
    padding: 0; 
}

/* commented backslash hack \*/ 
html, body {
    height:100%;
} 
/* end hack */

body {
    border-top: 2px solid #a89463;
    background-color: #fff;
}

#allcontainer {
    position: relative;
    top: 0;
    left: 50%;
    margin-left: -400px;
    width: 800px;
}

#main {
    position: relative;
    float: left;
    width: 100%;
    border-top: 3px double #ccc;
}

#contentcontainer {
    display: inline;
    float: right;
    padding: 20px 10px 25px 20px;
    margin-right: 10px;
    border-right: 1px solid #ccc;
    border-left: 1px solid #ccc;
}

#content {
    width: 600px;
}

/*------------------   Presentation styles   ------------------*/

h1, h2, h3, h4 {
    font-weight: bold;
    font-family: verdana, geneva, arial, sans-serif;
}

h1 {
    font: bold 25px/75px verdana, geneva, arial, sans-serif;
    text-align: center;
    letter-spacing: 4px;
}

h2 {
    display: block;
    font-size: 15px;
    text-decoration: underline;
    margin-bottom: 8px;
    padding: 3px 0;
    background-color: rgb(230,230,230);
}

#content p {
    font-family: verdana, arial, sans-serif;
    font-size: 13px;    
    line-height: 25px;
}

p.test {
  color: #ff0000;
}

#content p.location {
  color: #ff0080;
  line-height: 20px;
  
}

.centre {
    margin: 0 auto;
    text-align: center;
    margin-bottom: 10px;
}
        </xsl:text>
        </style>
      </head>
      <body>
        <div id="allcontainer">
          <div id="main">
            <div id="contentcontainer">
              <div id="content">
                <xsl:apply-templates />
              </div>
            </div>
          </div>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="svrl:failed-assert">
    <h2><xsl:value-of select="preceding-sibling::svrl:active-pattern[1]/svrl:title" /></h2>
    <p class="test">
      <xsl:value-of select="@test" />
    </p>
    <p class="location">
      <xsl:value-of select="@location" />
    </p>
    <p>
      <xsl:value-of select="svrl:text" />
    </p>
    <xsl:apply-templates />
  </xsl:template>


  <xsl:template match="*">
    <!-- drop these -->
  </xsl:template>
</xsl:stylesheet>
