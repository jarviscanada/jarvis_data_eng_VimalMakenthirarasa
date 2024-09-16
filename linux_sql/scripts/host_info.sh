#!/bin/bash
## Script usage
#./scripts/host_info.sh psql_host psql_port db_name psql_user psql_password
#
## Example
#./scripts/host_info.sh "localhost" 5432 "host_agent" "postgres" "mypassword"

# check if valid number of arguments
if [ "$#" -ne 5 ]; then
    echo "Illegal number of parameters"
    exit 1
fi
# assign CLI arguments to variables (e.g. `psql_host=$1`)
psql_host=$1
psql_port=$2
db_name=$3
psql_user=$4
psql_password=$5

# parse host hardware specifications and assign parsed outputs to variables
hostname=$(hostname -f)

lscpu_out=`lscpu`
cpu_number=$(echo "$lscpu_out"  | egrep "^CPU\(s\):" | awk '{print $2}' | xargs)
cpu_architecture=$(echo "$lscpu_out"  | egrep "Architecture:" | awk '{print $2}' | xargs)
cpu_model=$(echo "$lscpu_out" | egrep "Model name:" | awk '{$1=$2=""; print $0}' | xargs)
cpu_mhz=$(echo "$lscpu_out" | egrep "^BogoMIPS:" | awk '{print $2}' | xargs)
l2_cache=$(echo "$lscpu_out" | egrep "L2 cache:" | awk '{print $3}' | xargs)
timestamp=$(date +"%Y-%m-%d %H:%M:%S")
total_mem=$(vmstat --unit M | tail -1 | awk '{print $4}')

# construct the INSERT statement from specification variables
insert_statement="INSERT INTO host_info (hostname, cpu_number, cpu_architecture, cpu_model, cpu_mhz, l2_cache, timestamp, total_mem)
VALUES ('$hostname', $cpu_number, '$cpu_architecture', '$cpu_model', $cpu_mhz, $l2_cache, '$timestamp', $total_mem);"
#echo "$insert_statement"
# execute the INSERT statement through the psql CLI tool
export PGPASSWORD=$psql_password
psql -h "$psql_host" -p "$psql_port" -U "$psql_user" -d "$db_name" -c "$insert_statement"
#if [ $? -ne 0 ]; then
#    echo "Error: Failed to execute SQL command"
#    exit 1
#else
#    echo "Data inserted successfully."
#fi
