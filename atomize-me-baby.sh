#!/bin/sh
find . -name 'pom.xml' -execdir translate {} pom.atom \;
