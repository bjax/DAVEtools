# Creates the DAVEtools README pages
#
# 2011-07-29 Bruce Jackson, NASA Langley <bruce.jackson@nasa.gov>
# 2017-05-05 Changed to rely on asciidoc file <bruce@digiflightdyn.com>

PAGES       = README.html dave_demo.html

make: ${PAGES}

%.html : %.adoc
	asciidoc -a toclevels=3 -a data-uri -a icons $<
