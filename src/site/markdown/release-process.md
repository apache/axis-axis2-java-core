<!--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  -->

Release Process
===============

Release process overview
------------------------

### Cutting a branch

*   When a release is ready to go, release manager (RM) puts
    forward a release plan as per standard Apache process, including
    dates. This gets VOTEd on by the committers. During this period the
    trunk is still the only relevant source base.

*   As soon as a release is approved (or even before), RM should
    add the new version into JIRA as a target.

*   At the point where we would normally do the "code freeze" for a
    release, the RM cuts a branch named for the release. This branch is
    where the release candidates and releases will happen.

*   Ideally a release branch is only around for a week or maybe two
    before the release happens.

*   The only things that should EVER get checked into the release
    branch are - 1) bug fixes targeted at the release, 2)
    release-specific updates (documentation, SNAPSHOT removal, etc). In
    particular new functionality does not go here unless it is a
    solution to a JIRA report targeted at the release.

*   Normal development continues on the trunk.

### Dependencies and branches

*   The trunk should always be "cutting edge" and as such should
    usually be pointing at SNAPSHOT versions of all dependencies. This
    allows for continuous integration with our partner projects.

*   Soon after a release branch is cut, the RM is responsible for
    removing ALL dependencies on SNAPSHOT versions and replacing them
    with officially released versions. This change happens only on the
    release branch.

### Managing change and issue resolution with a release branch

*   The RM goes through JIRA issues and sets "fix for" to point to
    both "NIGHTLY" and the new branched release number for the fixes
    that are targeted for the release after the branch is cut.

*   In general, the assignee/coder fixes JIRA issues or makes other
    changes *on the trunk*. If the JIRA issue is targeted at the
    release, or upon coder's discretion, they then merge the fix over
    to the release branch.

*   This way the trunk is ALWAYS up-to-date, and we don't have to
    worry about losing fixes that have only been made on the release
    branch.

*   When the assignee resolves an issue, they confirm it's been
    fixed in both branches, if appropriate.

### Checking changes into the branch

*   If bug fixes are needed later for a release which has long
    since happened (to fix user issues, etc), those fixes generally
    should also happen on the trunk first assuming the problem still
    exists on the trunk.

*   There are only two cases where we would ever check anything
    into the branch without first checking it into the trunk. 1)
    Release specific items (release number references, release notes,
    removal of SNAPSHOTs), and 2) if the trunk has moved on in some
    incompatible way.

Performing a release
--------------------

**Note:** performing the release requires at least Maven 2.1.0. The recommended version is 2.2.1.

### Preparation

Verify that the code meets the basic requirements for being releasable:

1.  Check the consistency between the metadata in `pom.xml` and `modules/parent/pom.xml`.
    Since the root and parent POMs are different, some of the metadata is duplicated and needs to be synchronized
    manually. This includes the mailing list addresses, issue tracker information, SCM location, etc.

2.  Check that the set of legal (`legal/*.LICENSE`) files corresponds to the set of third party
    JARs included in the binary distribution.

3.  Check that the `apache-release` profile works correctly and produces the required distributions.
    The profile can be executed as follows:
    
        mvn clean install -Papache-release -Dmaven.test.skip=true

4.  Check that the source distribution is buildable.

5.  Check that the source tree is buildable with an empty local Maven repository.

If any problems are detected, they should be fixed on the trunk (except for issues specific to the
release branch) and then merged to the release branch.

Next update the relevant documents for the new release:

1.  Update the `release-notes.html` file on the release branch (since the content of this file is replaced
    with every release, there is no need to keep it in sync with the trunk, except if the template changes).

2.  Update the `src/site/xdoc/index.xml` file with a description of the release and add an entry for
    the release in `src/site/xdoc/download.xml`. To avoid extra work for the RM doing the next major release,
    these changes should be done on the trunk first and then merged to the release branch.
    If the release is a maintenance release, then the previous release from that branch must be prepared
    for archiving by changing the links in the download page. This is necessary to conform to the
    [release archiving policy](http://www.apache.org/dev/release.html#when-to-archive). If the release
    is a major release, then this should be done with the release from the oldest branch, unless it is expected
    that users will still continue to download and use that release.

### Pre-requisites

The following things are required to perform the actual release:

*   A PGP key that conforms to the [requirements for Apache release signing](http://www.apache.org/dev/release-signing.html).
    To make the release process easier, the passphrase for the code signing key should
    be configured in `${user.home}/.m2/settings.xml`:

        <settings>
          ...
          <profiles>
            <profile>
              <id>apache-release</id>
              <properties>
                <gpg.passphrase><!-- key passphrase --></gpg.passphrase>
              </properties>
            </profile>
          </profiles>
          ...
        </settings>

*   The release process uses a Nexus staging repository. Every committer should have access to the corresponding
    staging profile in Nexus. To validate this, login to [repository.apache.org](https://repository.apache.org)
    and check that you can see the `org.apache.axis2` staging profile. The credentials used to deploy to Nexus
    should be added to `settings.xml`:

        <servers>
          ...
          <server>
            <id>apache.releases.https</id>
            <username><!-- ASF username --></username>
            <password><!-- ASF LDAP password --></password>
          </server>
          ...
        </servers>

### Release

In order to prepare the release artifacts for vote, execute the following steps:

1.  Update the release date in `release-notes.html`, `src/site/xdoc/index.xml` and
    `src/site/xdoc/download.xml`. Since it is not possible to predict the exact date when the
    release is officially announced, this should be the date when the release tag is created.

2.  Temporarily disable the Hudson build(s) for Axis2, in order to avoid accidental deployment of the release
    candidate to the local repository of a Hudson executor if the release process fails somewhere in the middle and/or
    a Hudson build starts at the wrong moment.

3.  Start the release process using the following command:

        mvn release:prepare -Peverything

    When asked for a tag name, use the following format: `vX.Y.Z`. The `everything` profile
    makes sure that the version numbers of all Maven modules are incremented properly.
    The execution of the `release:prepare` goal may fail for users in
    locations that use the EU Subversion server. If this happens,
    wait for a minute (so that the EU server can catch up with its master) and simply rerun the command.
    It will continue where the error occurred.

4.  Perform the release using the following command:

        mvn release:perform

5.  Login to Nexus and close the staging repository. For more details about this step, see
    [here](https://docs.sonatype.org/display/Repository/Closing+a+Staging+Repository).

6.  Deploy the distributions to your `public_html` area on `people.apache.org`.
    The `release:perform` goal should have produced all the necessary files in the
    `target/checkout/target/axis2-&lt;version&gt;-dists` folder. Please preserve the directory structure and
    file names because they exactly match the requirements for deployment to `www.apache.org`
    (see below).

7.  Generate and deploy the Maven site to your `public_html` area on `people.apache.org`
    (either by building the site locally and transfer the files to `people.apache.org`, or by
    checking out the release tag and building the site directly on `people.apache.org`).

8.  Start the release vote by sending a mail to `java-dev@axis.apache.org`.
    The mail should mention the following things:

    *   The list of issues solved in the release (by linking to the relevant JIRA view).
    *   A link to the Nexus staging repository.
    *   The URL on `people.apache.org` where the distributions can be downloaded.
    *   A link to the preview of the Maven site.

9.  Reenable the Hudson build(s).

If the vote passes, execute the following steps:

1.  Promote the artifacts in the staging repository. See
    [here](https://docs.sonatype.org/display/Repository/Releasing+a+Staging+Repository)
    for detailed instructions for this step.

2.  Login to `people.apache.org` and copy the distributions (including the checksums and
    signatures) to `/www/www.apache.org/dist/axis/axis2/java/core/`. If you followed the
    steps described above, then you should already have everything that is needed in your `public_html`
    folder and you only need to copy the `X.Y.Z` folder to the right location. Please execute the
    copy with umask 0002 and check that the files and directories have the right permissions (write access for the
    `axis` group).

3.  Check out the current Axis2 site from <https://svn.apache.org/repos/asf/axis/site/axis2/java/core/>
    and synchronize it with the site for the new release. The site should have been generated during the
    release build and can be found in the `target/checkout/target/site`. Alternatively you can
    check out the release tag and rebuild the site using `mvn site`, or extract it from the
    documents distribution. Axiom has a script (see `etc/syncsite.py`) that can be used to
    synchronize the site in Subversion. It takes care of executing the relevant `svn add`
    and `svn remove` commands on the local working copy of the site.
    The live Web site is updated automatically by svnpubsub once the changes have been committed to SVN.

It may take several hours before everything has been synchronized. Before proceeding, check that

*   the Maven artifacts for the release are available from the Maven central repository;
*   the Maven site has been synchronized;
*   the distributions can be downloaded from the mirror sites.

Once everything is in place, send announcements to `java-user@axis.apache.org` (with copy to
`java-dev@axis.apache.org`) and `announce@apache.org`. Since the two lists have different conventions,
audiences and moderation policies, it is recommended to send the announcement separately to the two lists.
Note that mail to `announce@apache.org` must be sent from an `apache.org` address and will
always be moderated. The announcement sent to `announce@apache.org` also should include a general description
of Axis2, because not everybody subscribed to that list knows about the project.

### Post-release actions

1.  Update the DOAP file (`etc/doap_Axis2.rdf`) and add a new entry for the release.

2.  Update the status of the release version in JIRA.

3.  Remove archived releases from `/www/www.apache.org/dist/axis` on `people.apache.org`.
