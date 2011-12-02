<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:cml="http://www.xml-cml.org/schema"
                xmlns="http://www.w3.org/1999/xhtml" 
                xmlns:h="http://www.w3.org/1999/xhtml"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                version="2.0">
  
  <xsl:output method="xml"
               omit-xml-declaration="no"
               indent="yes"/>            

  <xsl:template match="/">
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="iso:schema">
    <xsl:text disable-output-escaping="yes">
    <![CDATA[<!DOCTYPE html 
     PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
     "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">    
    ]]>
    </xsl:text>
    <html>
      <head>
        <title>
          <xsl:choose>
            <xsl:when test="dc:title">
              <xsl:value-of select="dc:title" />              
            </xsl:when>
            <xsl:otherwise>
            schematron documentation
            </xsl:otherwise>
          </xsl:choose>
        </title>
        <link rel="schema.DC" href="http://purl.org/dc/elements/1.1/" />
        <link rel="schema.DCTERMS" href="http://purl.org/dc/terms/" />
        <xsl:apply-templates select="dc:*" />
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
    margin-left: -405px;
    width: 810px;
}

#topheader {
    position: relative;
    width: 810px;
    margin: 10px auto 0;
    height: 60px;   
}

#bottomheader {
    position: relative;
    width: 810px;
    margin: 6px auto 0;
    border-top: 3px double #ccc;
    height: 60px;
    background: rgb(242,242,242) center center no-repeat;
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

#menu {
    display: inline;
    float: left;
    width: 150px;
    margin-left: 10px;
}

#footer {
    position: relative;
    width: 800px;
    height: 30px;
    border-top: 3px double #ccc;
    text-align: left;
    padding-top: 9px;
    margin: 0 auto;
    text-align: center;
    clear: both;        
}

div.imageholder {
    margin: 15px auto;
    text-align: center;
}

table {
    border-collapse: collapse;
    font-family: verdana, geneva, arial, sans-serif;
    color: #222;
    margin-bottom: 20px;
    font-size: 11px;
    line-height: 20px;
}

tr {
    border: 1px solid black;
}

td {
    border: 1px solid black;
    padding: 0 3px 0;
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

p, span.ulheader {
    font-family: verdana, geneva, arial, sans-serif;
    color: #222;
    margin: 0;
    padding: 0 0 15px 0;
    font-size: 11px;
    line-height: 20px;
}

#footer p {
    font-family: verdana, geneva, arial, sans-serif;
    text-transform: uppercase;
    margin: 0px 0px 2px 0px;
    font-size: 10px;
    line-height: 10px;
    letter-spacing: 2px;
}

a, a:link, a:visited, a:hover, a:active {
    color: #222;    
}

a img.universityofcambridge {
    display: inline;
    margin-top: 15px;
    float: left;
    width: 218px;
    border: none;
}

a img.unilevercentre {
    display: inline;
    float: right;
    width: 183px;
    border: none;   
}

p.breadcrumbs {
    font-size: 10px;
}

#content p {
    font-family: verdana, arial, sans-serif;
    font-size: 13px;    
    line-height: 20px;
}

div.assert-rule p.test {
  color: #ff0000;
}

div.report-rule p.test {
  color: #00ff00;
}

div.question {
  font-style: italic;
}

span.math {
    font-family: serif;
    font-style: italic;
    font-size: 13px;    
    line-height: 20px;
}

.centre {
    margin: 0 auto;
    text-align: center;
    margin-bottom: 10px;
}

/*------------------   List styles   ------------------*/

ul.normal {
    margin: 0 0 15px 40px;
}

ul.normal li {
    font-family: verdana, arial, sans-serif;
    font-size: 13px;    
    line-height: 20px;
    list-style-type: square;
}
        </xsl:text>
        </style>
      </head>
      <body>
        <xsl:apply-templates />
      </body>
    </html>
  </xsl:template>
   
  <xsl:template match="dc:*">
    <meta>
      <xsl:attribute name="name">DC.<xsl:value-of select="local-name()" /></xsl:attribute>
      <xsl:attribute name="content"><xsl:value-of select="." /></xsl:attribute>
    </meta> 
  </xsl:template>
  
  
  <xsl:template match="iso:title">
    <h2>
      <xsl:value-of select="." />
    </h2>
  </xsl:template>
  
  <xsl:template match="iso:p">
    <p class="schema-p">
      <xsl:apply-templates select="*"/>
    </p>
  </xsl:template>
  
  <!-- makes all xhtml namespaced elements non-namespaced -->
  <xsl:template match="h:*">
    <xsl:element name="{local-name()}">
    <xsl:for-each select="@*">
      <xsl:attribute name="{local-name()}">
        <xsl:value-of select="." />
      </xsl:attribute>
    </xsl:for-each>
    <xsl:apply-templates select="node()"/>
    </xsl:element>
  </xsl:template>
 
  <xsl:template match="iso:pattern">
    <div class="pattern">
      <xsl:apply-templates /> 
    </div>
  </xsl:template>
  
  <xsl:template match="iso:pattern/iso:title">
    <h3>
      <xsl:value-of select="." /> (context = <xsl:value-of select="../iso:rule/@context" />)      
    </h3>
  </xsl:template>
  
  <xsl:template match="iso:pattern/iso:p">
    <p class="pattern-p">
      <xsl:apply-templates select="*"/>
    </p>
  </xsl:template>
 
  <xsl:template match="iso:pattern/iso:rule">
    <xsl:for-each select="iso:assert|iso:report">
      <div>
        <xsl:attribute name="class">
        <xsl:choose>
          <xsl:when test="local-name()= 'assert'">assert-rule</xsl:when>
          <xsl:when test="local-name()= 'report'">report-rule</xsl:when>
          <xsl:otherwise>rule</xsl:otherwise>
        </xsl:choose>
        </xsl:attribute>
        <p class="test">
          <xsl:value-of select="@test" />
        </p>
        <p class="message">
          <xsl:value-of select="." />
        </p>
        <xsl:if test="preceding-sibling::*[1][local-name()='p']">
          <div class="reason">
            <xsl:apply-templates select="preceding-sibling::*[1]" />
          </div>
        </xsl:if>
      </div>
    </xsl:for-each>
    
  </xsl:template>
 
  
  <xsl:template match="*">
    <!-- drop these -->
  </xsl:template>
  
</xsl:stylesheet>