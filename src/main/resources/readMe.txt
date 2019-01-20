Each Scenario must contain the following json files:

summary.json
alliesTaskForces.json
axisTaskForces.json

Each Scenario must contain a image file.
The Scenario image size must be: 240 x 180.

Each Scenario must reference two map files. The map files are located in

    /resources/maps/{Date}/alliesBases.json
    /resources/maps/{Date}/axisBases.json


Map files define each sides bases.
Map files define each sides regions.
Aircraft deployment is limited by region. The region JSON files aircraft deployment numbers are in steps.
    Each region may have a minimum number of steps.
    Each region may have a maximum number of steps.
