@echo off

echo cleaning 
del .\xsl\cml-lite.xsl

echo building xsl/cml-lite.xsl
java -jar y:/saxon/saxon9.jar -o:./xsl/cml-lite.xsl -s:cml-lite.sch -xsl:./xsl/iso_svrl.xsl

echo generating documentation
java -jar y:/saxon/saxon9.jar -o:./doc/index.html -s:cml-lite.sch -xsl:./xsl/extract-doc.xsl