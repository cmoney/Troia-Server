============================
Test data generator tutorial
============================

What is topic of this tutorial
==============================
This tutorial is written to help you understand how
to use test data generator to either generate test data 
files or as a part of your program.

Test data generator installation
================================
To install this software you will need following programs 

- git - http://git-scm.com
- maven - http://maven.apache.org/

After setting up environment you need to build jar file by running a command from troia-data-generator directory:
::

 mvn clean package

This will generate executable jar in *target* directory.

How to generate data files from command line
============================================

GAL
---
To generate test data you must call executable jar from command line, so
minimal command will look like this
::

 java -cp target/troia-data-generator-0.5.jar com.datascience.gal.dataGenerator.TroiaDataGenerator

This will display help that lists available parameters witch
are :

- f - Loads configuration from settings file
- t - base name for output files
- c - number of categories in test data
- o - number of objects in test data
- w - number of workers in test data
- h - maximal quality of worker (from 0 to 1)
- l - minimal quality of worker (from 0 to 1)
- p - number of workers assigned to single object
- g - ratio of gold labels among objects (from 0 to 1)
- q -  name of file containing basic workers definition.


After setting up all parameters generator will create four files.

- basename_aiworker.json
- basename_goldLabels.txt
- basename_labels.txt
- basename_objects.txt

Where basename is string given with parameter -t 

basename_aiworker.json
^^^^^^^^^^^^^^^^^^^^^^
This file contains collection of workers with confusion matrices in
JSON format. Structure of those classes is following :
Collection<AiWorker> where AiWorker contains two fields String name;
and ConfusionMatrix matrix. Confusion matrix is simply Map<String,Map<String,Double>>

basename_goldLabels.txt
^^^^^^^^^^^^^^^^^^^^^^^
Contains gold labels in format 
::

 <object name><tabulation><category name>

basename_labels.txt
^^^^^^^^^^^^^^^^^^^
Contains labels assigned to objects by artificial workers
This file is formatted in following 
::

 <worker name><tabulation><object name><tabulation><category name>

basebame_objects.txt
^^^^^^^^^^^^^^^^^^^^
Contains objects that are included in test, each of those objects
have correct category assigned so it will be possible to compute
amount of correct labels generated by AI workers or Troia.
Format of this file is as follows :
::
 
 <object name><tabulation><category name>

Configuration file
^^^^^^^^^^^^^^^^^^
If you don't want to enter test parameters from console you can use
configuration file. It is simple Java .properties file that contains
all parameters required for generating test data. 
::

 category_count = 3
 object_count = 2000
 worker_count = 30
 minimal_worker_quality = 0
 maximal_worker_quality = 1
 workers_per_object = 3
 gold_ratio = 0.1

To use this file you need to call generator with -f as a parameter
with name of .properties file as a value , for example
::
  
 TroiaDataGenerator -f test.properties

GALC
----

To generate test data you must call executable jar from command line, so
minimal command will look like this
::

 java -cp target/troia-data-generator-0.5.jar com.datascience.galc.dataGenerator.Main

This will display help that lists available parameters witch
are :

- --evalObjects <evalobjectsfile>
- --evalWorkers <evalworkersfile>
- --gold <goldfile>
- --labels <labelsfile>
- --synthetic <syntheticoptionsfile>
- --verbose <verbose>

Synthehic file
^^^^^^^^^^^^^^

File contains options for the creation of new data. Example of this file may look like this
::

 data_points=100
 data_mu=7.0
 data_sigma=11.0
 data_gold=100
 workers=2
 worker_mu_down=-5.0
 worker_mu_up=5.0
 worker_sigma_down=0.5
 worker_sigma_up=1.5
 worker_rho_down=0.5
 worker_rho_up=1.0
