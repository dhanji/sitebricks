Sitebricks
----------

Sitebricks is a simple set of libraries for web applications. Sitebricks focuses on early error
 detection, low-footprint code, and fast development. Powered by
 [Guice](http://code.google.com/p/google-guice), it also balances idiomatic
 Java with an emphasis on concise code.

### Early error detection ###

This following misspelling results in a template compile error:

    <body>
         ${person.naem}
    All such errors are picked up early and reported at once in a format similar to javac.

    1) unknown or unresolvable property: naem

     15:      <body>
     16:          ${person.naem}</li>
                              ^

    Total errors: 1

### Next steps ###

Get started
5-minute tutorial
Building RESTful web services

* * *

We would love your contributions and help in developing Sitebricks at this early stage. If you'd
like to contribute or have any questions, please join the [mailing list](http://groups.google.com/group/google-sitebricks).

Or send us a note on twitter [@dhanji](http://twitter.com/dhanji) =)