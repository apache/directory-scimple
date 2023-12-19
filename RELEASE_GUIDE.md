# Apache Directory SCIMple Release Guide

> **NOTE:** This guide is a work in progress.

## Maven Settings
You’ll need a settings section for repository.apache.org Here’s what an `~/.m2/settings.xml` file might look like:
```xml
<settings>
  <servers> 
    <!-- To stage a release of some part of Maven -->
    <server>
      <id>apache.releases.https</id>
      <username>username</username>
      <password>********</password>
    </server>
  </servers>    
</settings>
```

Just replace your username and passwords. 
> **NOTE: The username and password is your Apache LDAP account.

## GPG Key

All subprojects are configured to deploy signatures for the artifacts uploaded to the repository. The gpg plugin will check use the default gpg key for the user deploying the project with the `release:perform` directive of the release plugin. This will prompt you for the passphrase for the default key. If you do not have one set up the build will fail.

You can generate and upload a PGP key to a PGP keyserver using the following commands:

```shell
gpg --gen-key
gpg --fingerprint
gpg --keyserver subkeys.pgp.net --send-keys <your key's id from last command>
Make sure to have created the .pgpkey in your p.a.o/~ directory and to have added your public key to the KEYS file. See also http://people.apache.org/~henkp/repo/faq.html#4
```

> **NOTE:** You can also store a [GPG key on a Yubikey](https://developer.okta.com/blog/2021/07/07/developers-guide-to-gpg).

## Release Process
Since we are using Nexus for the release process is as follows (see also [Publishing maven artifacts](https://www.apache.org/dev/publishing-maven-artifacts.html#staging-maven)).

### Test the Project
```shell
$ mvn release:prepare -DdryRun=true
```

Be aware that this phase will ask you about the next version, and most important, for the next SCM tag :

```text
...
[INFO] Checking dependencies and plugins for snapshots ...
What is the release version for "Apache Directory SCIMple"? (org.apache.directory.scimple:scimple) 1.0.0-M1: :
...
What is SCM release tag or label for "Apache Directory SCIMple"? (org.apache.directory.scimple:scimple) 1.0.0-M1: :
...
```

### Prepare the Release

```shell
$ mvn release:clean
$ mvn release:prepare
```

This creates a [git tag](https://github.com/apache/directory-scimple).

### Stage the Release

```shell
$ mvn release:perform
```

This deploys the release to a staging repository.

## Publish Source and Binary Distribution Packages
The sources, binaries and their signatures, have to be pushed in a place where they can be downloaded by the other committers, in order to be checked while validating the release.

If you haven’t checked out this space, do it now :

```shell
$ svn co https://dist.apache.org/repos/dist/dev/directory/scimple <directory-scimple-dist-dir>
```

That will checkout the project distributions.

Now, create a sub-directory for the version you have generated (here, for version 1.0.0-RC1) :

```shell
$ mkdir ~/apacheds/dist/dev/directory/scimple/1.0.0-M1
```

Copy the packages and signature to this area:

```shell
$ cd distributions/target
$ cp scimple-<version>-* <directory-scimple-dist-dir>/1.0.0-M1
```

Last, not least, commit your changes

```shell
$ svn add <directory-scimple-dist-dir>/1.0.0-M1
$ svn ci <directory-scimple-dist-dir>/1.0.0-M1 -m "Apache SCIMple 1.0.0-M1"
```

## Vote
Start a 72h vote at the dev mailing list.

## Release
If the vote succeeds the project can be released.

Go to https://repository.apache.org/index.html#stagingRepositories and release the staging repository so all artifacts are published to Maven central.

The sources, binaries and their signatures, have to be pushed in a place where they can be downloaded by users. We use the [distribution](https://dist.apache.org/repos/dist/release/directory/scimple) space for that purpose.

Move the distribution packages (sources and binaries) to the dist SVN repository: https://dist.apache.org/repos/dist/release/directory/scimple/dist/${RELEASE}

```shell
$ svn mv https://dist.apache.org/repos/dist/dev/directory/scimple/${RELEASE} https://dist.apache.org/repos/dist/release/directory/scimple.dist/ -m "Adding SCIMple Release ${RELEASE}"
```

## Deploy the Javadocs

```shell
$ cp target/checkout/target/apidocs TODO
```

## Update the web site

TODO

## Update Apache Reporter

Add release to https://reporter.apache.org/addrelease.html?directory

## Inform the world !

After 24h, you can now inform the world about the release.

Send a mail to the users and dev mailing list, and one to the [announce@apache.org](mailto:announce@apache.org)

You are done !
