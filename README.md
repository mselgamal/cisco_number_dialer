# Cisco Number Dialer

tool for auto dialing numbers in Cisco environment. The tool accomplishes the following:

- prompt user for api admin and user
- prompt cucm server address
- prompt user for a list of numbers, enter file you prepared "pre-testing"
- prompt for thread count
- for each number in the list ## thread is spawned for each iteration ##
  - call the number
  - wait until session is established or call timed-out or call failure
  - wait 1 sec
  - disconnect and continue to next number
    - if the call failed add to failed calls list
    - if the call timed out add to timed out list
  - print any failed calls

### Prerequisites

Java version: Java 1.8 or Java 1.7.0.79
Systems: CUCM 11.x and UCCX

- Create CTI ports in UCCX (call control group), these ports determine the number of simultaneous call
  - 1 cti port = 1 call at a time (program runs 1 call at a time, slowest runtime)
- Create a end user in cucm with following privileges
    - Standard CTI Allow Control of Phones supporting Connected Xfer and conf
    - Standard CTI Allow Control of All Devices
    - Standard CTI Enabled
- Add cti ports to "Controlled Devices"
- The numbers used for testing should be pointing to voicemail/Auto Attendant/Auto Answer Phone, but not required.
When the numbers are routed to voicemail/AA, runtime improves significantly.
- prepare a csv file that lists every number getting ported. Otherwise prepare a file with 100 numbers to test. The numbers should be listed as Access-code+1+10-digits and whether its internal or
external. i.e “912487878187,external” or “913053412733,internal”
  - internal -> the number exists on a PBX system on company's network
  - external -> any number on the PSTN
- The Script will take approx. 25 secs per number worst case.

## Deployment

download jar file under multi_threaded_ver branch

cisco-number-dialerMT.jar

## Built With

* [Java](https://www.oracle.com/java/)

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags).

## Authors

* **Mamdouh Elgamal**

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details
