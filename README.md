## [Sitebricks](http://sitebricks.documentnode.com/)

Sitebricks is a simple set of libraries for web applications. Sitebricks focuses on early error
 detection, low-footprint code, and fast development. Powered by
 [Guice](https://github.com/google/guicee), it also balances idiomatic
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


- [Next steps](http://sitebricks.documentnode.com/)
- [Mailing list](http://groups.google.com/group/google-sitebricks)


## eGym Maintenance Branch

This (<https://github.com/egymgmbh/sitebricks/tree/egym>) is the [eGym](https://egym.de) maintenance branch, where the eGym developer
team fixes some Sitebricks bugs. This is necessary due to lack of response to pull request from the original developers.

### How to build

First you need to have JDK 7 and Maven 3 installed on your system.

To build the Sitebricks core (which is the only part used by eGym) just type:

```
./build_core.sh
```

If you want run the acceptance tests as well:

```
mvn clean install -am -pl :sitebricks-acceptance-tests
```

### How to pull changes from upstream

If you want to upgrade the eGym fork of Sitebricks with upstream master branch changes
you can do the following:

First checkout the local master branch and then ensure that there is an upstream remote,
if `git remote -v` doesn't have an upstream listed, you can do the following to add it:

```
git remote add upstream https://github.com/dhanji/sitebricks
```

Then pull in the latest changes from upstream and push them to the eGym fork:

```
git pull upstream master
git push origin master
```

After that checkout the `egym` branch and merge it with the `master` branch.

Don't forget to update the version in each `pom.xml` file after that.
