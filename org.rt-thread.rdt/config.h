// <!DOCTYPE CodeConfigurator "http://www.rt-thread.com/tools">
#ifndef __CONFIG_H__
#define __CONFIG_H__

// <section SECTION1 description="section desciption" default="always" >
// <bool BOOL_TYPE1 default="true" >
//#define BOOL_TYPE1
// <bool BOOL_TYPE2 default="false" >
#define BOOL_TYPE2

// <string STRING_NAME default="this is a string" >
// <choose STRING_NAME default="0" >
// <item>"this is a string"</item>
// <item>"this"</item>
// <item>"is"</item>
#define STRING_NAME "this is a string"
// </choose>

// <string STRING_NAME1 default="ffff" >
#define STRING_NAME1

// <integer CHOOSE_NAME1 default="8" range=0:100>
#define CHOOSE_NAME1    8
// <choose CHOOSE_NAME1 default="1" >
// <item>1</item>
// <item>2</item>
// <item>3</item>
#define CHOOSE_NAME1 1
// </choose>

// </section>

// <integer int_test description="test and test" depend="SECTION2" default="3" range=0:100>
#define int_test 2
// <integer int_test1 description="test and test" depend="SECTION2" default="3" range=0:100>
#define int_test 4
// <integer int_test2 description="test and test" depend="SECTION2" default="30" range=0:100>
#define int_test 100

// <section SECTION2 description="section2" default="true" >
#define SECTION2
// <bool BOOL_TYPE3 default="true" >
#define BOOL_TYPE3
// <bool BOOL_TYPE4 default="true" >
#define BOOL_TYPE4
// </section>

#endif
