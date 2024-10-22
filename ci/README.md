# Java Chassis Code Checks

* Compilation and Installation

   see .github/workflows/maven.yml

* Checkstyle

  see .github/workflows/checkstyle.yml

* Rat Check

  see .github/workflows/rat_check.yml

* Spot Bugs

  see .github/workflows/spotbugs.yml

* OWASP Dependency Check

  `mvn verify  -Powasp-dependency-check` . Very Slow, run manually.

* Distribution

  `mvn clean deploy -Dcheckstyle.skip -Dspotbugs.skip=true -Dmaven.javadoc.skip=true -DskipTests -Prelease -Pdistribution` . Run manually when preparing a release.
