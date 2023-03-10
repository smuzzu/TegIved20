# Some developement notes

* I am using java 8, Maven, TestNG and Apache Httpcomponents on this project.
Httpcomponents is a fast and reliable java lib f or rest api test and some web scrapping scenarios as well.

* TestSuite1.java contains 4 automatic test

* .github/workflows/buildProject.yml was added to github to build and run tests after every commit (CI/CD)

* After ruining the tests you can check results in a simple html interface: target/surefire-reports/index.html.  I am attaching two sample screenshots on src/doc folder

* As Martian Solar days lasts 24 hours, 39 minutes and 35 seconds I am not a space expert but I think It may be some scenarios where a Martian day will fit in two pieces of a Earth day. I tried Martian day 1000 and api results overlaps 100% with 2015-05-30 on earth day therefore for the purpose of this test I will ignore the posibility of having a Martian Sol day in two pieces of consecutive Earth days.

* Assert.assertEqualsNoOrder clould have been used on test 1, 2 and 3 but I preferred to loop every value to provide more precise information about the exact photo that makes the test fail

* I reused some test assets and improve time spent on suite run.
I improved test 3 and 4 reusing data from previous API request to make all the suite run faster

* Test 1,2 and 3 will succeed but **test 4 will fail in a consistent way** because "MAST" camera have far more than 10 times photos taken than the other ones on Martian Sol day 1000, as follows:

  MAST -> 859
  
  NAVCAM -> 10
  
  RHAZ ->  4
  
  FHAZ ->  4
  
  CHEMCAM -> 4

* With some more time I would like to improve the HttpLib adding some logging (log4J) and improving javadoc and exception handling

* I am open to any suggestions, improvements. 
