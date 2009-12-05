require 'buildr'

VERSION_NUMBER = '0.8-SNAPSHOT'

repositories.remote << 'http://www.ibiblio.org/maven2'
repositories.remote << 'http://nexus.openqa.org/content/repositories/releases'
repositories.remote << 'http://repository.codehaus.org'


# Test deps
JUNIT = 'junit:junit:jar:3.8.1'
TESTNG = 'org.testng:testng:jar:jdk15:5.8'
EASYMOCK = 'org.easymock:easymock:jar:2.4'

# Compile deps
MVEL = transitive('org.mvel:mvel2:jar:2.0.14')
GUICE_SERVLET = 'com.google.inject.extensions:guice-servlet:jar:2.0'
GOOGLE_COLLECTIONS = transitive('com.google.collections:google-collections:jar:1.0-rc2')
JCIP = transitive('net.jcip:jcip-annotations:jar:1.0')
INTELLIJ_ANNO = transitive('com.intellij:annotations:jar:7.0.3')
GUICE = transitive('com.google.inject:guice:jar:2.0')
COMMONS_HTTPC = transitive('commons-httpclient:commons-httpclient:jar:3.1')
COMMONS_IO = transitive('commons-io:commons-io:jar:1.4')
DOM4J = transitive('dom4j:dom4j:jar:1.6.1')
JAXEN = transitive('jaxen:jaxen:jar:1.1.1')
SAXPATH = transitive('saxpath:saxpath:jar:1.0-FCS')
SERVLET_API = transitive('javax.servlet:servlet-api:jar:2.5')

# Acceptance test deps
JETTY  = group('jetty', 'jetty-util',
               :under=>'org.mortbay.jetty', :version=>'6.1.21')
JETTY_API = 'org.mortbay.jetty:servlet-api-2.5:jar:6.1.9'
SELENIUM_WEBDRIVER = transitive(group('webdriver-common', 'webdriver-htmlunit', 'webdriver-support',
                                      :under=>'org.seleniumhq.webdriver', :version=>'0.9.7376'))
COMMONS_COLLECTIONS = transitive('commons-collections:commons-collections:jar:20040616')

# Dependency shortcuts
COMPILE_DEPS = [MVEL, GUICE, GUICE_SERVLET, GOOGLE_COLLECTIONS, JCIP, INTELLIJ_ANNO, COMMONS_HTTPC,
                COMMONS_IO, DOM4J, JAXEN, SERVLET_API]
TEST_DEPS = [EASYMOCK, JUNIT, TESTNG]
ACCEPTANCE_TEST_DEPS = [JETTY, JETTY_API, TESTNG, SELENIUM_WEBDRIVER, COMMONS_COLLECTIONS]

define 'sitebricks-parent' do
  project.version = VERSION_NUMBER
  project.group = 'com.google.sitebricks'
  compile.options.target = '1.5'
  compile.options.source = '1.5'

  desc 'Sitebricks source'
  define 'sitebricks' do
    test.with TEST_DEPS
    test.using :fail_on_failure=>false
    test.using :testng

    compile.with COMPILE_DEPS, TEST_DEPS
    package :jar, :id=>'sitebricks'
  end

  desc 'Acceptance tests'
  define 'acceptance-test' do
    compile.with ACCEPTANCE_TEST_DEPS, COMPILE_DEPS, projects('sitebricks')
    test.with ACCEPTANCE_TEST_DEPS
    test.using :testng
  end
end
