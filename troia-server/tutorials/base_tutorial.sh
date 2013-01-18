#!/bin/bash

#URL="http://localhost:8080/troia-server-0.8"
URL="http://project-troia.com/api"
JobID="test_1"

#create the job
echo "Creating a new job ..."
result=$(curl -s1 -X POST -H "Content-Type: application/json" "$URL/jobs" -d "id=$JobID&type=incremental&categories=[
{"prior":"1","name":"porn","misclassification_cost":{"porn":"0","notporn":"1"}},
{"prior":"1","name":"notporn","misclassification_cost":{"porn":"1","notporn":"0"}}]")

status=$(echo $result| cut -d ',' -f 3 | cut -d ':' -f 2 | cut -d '"' -f 2)

if [[ "$status" != "OK" ]]
  then
    echo "Job with id $JobID already exists"
  else
    echo "Created new job with id $JobID"
fi
echo "-----------------"


#upload assigned labels
echo "Uploading assignedLabels ..."
result=$(curl -s1 -X POST -H "Content-Type: application/json" "$URL/jobs/$JobID/assignedLabels" -d 'labels=[
{"workerName":"worker1","objectName":"http://sunnyfun.com","categoryName":"porn"},
{"workerName":"worker1","objectName":"http://sex-mission.com","categoryName":"porn"},
{"workerName":"worker1","objectName":"http://google.com","categoryName":"porn"},
{"workerName":"worker1","objectName":"http://youporn.com","categoryName":"porn"},
{"workerName":"worker1","objectName":"http://yahoo.com","categoryName":"porn"},
{"workerName":"worker2","objectName":"http://sunnyfun.com","categoryName":"notporn"},
{"workerName":"worker2","objectName":"http://sex-mission.com","categoryName":"porn"},
{"workerName":"worker2","objectName":"http://google.com","categoryName":"notporn"},
{"workerName":"worker2","objectName":"http://youporn.com","categoryName":"porn"},
{"workerName":"worker2","objectName":"http://yahoo.com","categoryName":"porn"},
{"workerName":"worker3","objectName":"http://sunnyfun.com","categoryName":"notporn"},
{"workerName":"worker3","objectName":"http://sex-mission.com","categoryName":"porn"},
{"workerName":"worker3","objectName":"http://google.com","categoryName":"notporn"},
{"workerName":"worker3","objectName":"http://youporn.com","categoryName":"porn"},
{"workerName":"worker3","objectName":"http://yahoo.com","categoryName":"notporn"},
{"workerName":"worker4","objectName":"http://sunnyfun.com","categoryName":"notporn"},
{"workerName":"worker4","objectName":"http://sex-mission.com","categoryName":"porn"},
{"workerName":"worker4","objectName":"http://google.com","categoryName":"notporn"},
{"workerName":"worker4","objectName":"http://youporn.com","categoryName":"porn"},
{"workerName":"worker4","objectName":"http://yahoo.com","categoryName":"notporn"},
{"workerName":"worker5","objectName":"http://sunnyfun.com","categoryName":"porn"},
{"workerName":"worker5","objectName":"http://sex-mission.com","categoryName":"notporn"},
{"workerName":"worker5","objectName":"http://google.com","categoryName":"porn"},
{"workerName":"worker5","objectName":"http://youporn.com","categoryName":"notporn"},
{"workerName":"worker5","objectName":"http://yahoo.com","categoryName":"porn"}]')

status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
redirect=$(echo $result| cut -d ',' -f 3 | cut -d ':' -f 2 | cut -d '"' -f 2)

if [[ "$status" != "OK" ]]
  then
    echo "Upload assigned labels failed with status $status"
    exit 1
  else
    echo "Uploaded successfully the assigned labels"
fi


#check that the assigned labels were uploaded successfully
echo "Getting assigned labels job status for redirect=$redirect ..."
result=$(curl -s1 -X GET "$URL/jobs/$JobID/status/$redirect")
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)

if [[ "$status" != "Assigns added" ]]
  then
    echo "Get assigned labels failed with status $status"
    exit 1
  else
    echo "Got successfully job status for assigned labels "
fi
echo "-----------------"


#load gold labels
echo "Loading the gold labels ..."
result=$(curl -s1 -X POST -H "Content-Type: application/json" "$URL/jobs/$JobID/goldData" -d 'labels=
[{
    "correctCategory": "notporn",
    "objectName": "http://google.com"
}]')
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
if [[ "$status" != "OK" ]]
  then
    echo "Loading gold labels failed with status $status"
    exit 1
  else
    echo "Loaded successfully the gold labels"
fi
echo "-----------------"


#compute 
echo "Computing - using 20 iterations ..."
result=$(curl -s1 -X POST -d "iterations=20" "$URL/jobs/$JobID/compute")
redirect=$(echo $result| cut -d ',' -f 3 | cut -d ':' -f 2 | cut -d '"' -f 2)
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
if [[ "$status" != "OK" ]]
  then
    echo "Data computation failed with status $status"
    exit 1
  else
    echo "Data computation finished successfully"
fi
echo "-----------------"

echo $redirect

#get the job status and check that the data is correct
echo "Checking if the computation has ended - redirect=$redirect..."
result=$(curl -s1 -X GET "$URL/jobs/$JobID/status/$redirect")
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
while [[ $status != "Computation done" ]]
  do
    echo The status is $status - waiting 5 seconds
    sleep 5 
    result=$(curl -s1 -X GET "$URL/jobs/$JobID/status/$redirect")
    status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
  done
echo $status
echo "-----------------"


#get the workers score
echo "Getting the workers score ... "
result=$(curl -s1 -X GET "$URL/jobs/$JobID/prediction/workersScore")
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
if [[ "$status" != "OK" ]]
  then
    echo "Get workers score job failed with status $status"
    exit 1
  else
    echo "Get workers score job finished successfully"
fi
echo "-----------------"

#get the workers quality
echo "Getting the workers quality ... "
result=$(curl -s1 -X GET "$URL/jobs/$JobID/prediction/workersQuality")
redirect=$(echo $result| cut -d ',' -f 3 | cut -d ':' -f 2 | cut -d '"' -f 2)
status=$(echo $result| cut -d ',' -f 2 | cut -d ':' -f 2 | cut -d '"' -f 2)
if [[ "$status" != "OK" ]]
  then
    echo "Get workers quality job failed with status $status"
    exit 1
  else
    echo "Get workers quality job finished successfully"
fi

echo "Checking the status of the job with redirect=$redirect ..."
result=$(curl -s1 -X GET "$URL/jobs/$JobID/status/$redirect")
echo $result

