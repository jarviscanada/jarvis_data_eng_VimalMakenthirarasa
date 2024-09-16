#!/bin/bash
## Script usage
#bash scripts/host_usage.sh psql_host psql_port db_name psql_user psql_password
#
## Example
#bash scripts/host_usage.sh localhost 5432 host_agent postgres password

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

# save machine stats (MB) and current machien hostname to variables
vmstat_mb=$(vmstat --unit M)
hostname=$(hostname -f)

# retrieve hardware specification variables
memory_free=$(echo "$vmstat_mb" | awk '{print $4}'| tail -n1 | xargs)
cpu_idle=$(echo "$vmstat_mb"| tail -1 | awk '{print $15}') 
cpu_kernel=$(echo "$vmstat_mb"| tail -1 | awk '{print $14}')
disk_io=$(echo "$vmstat_out"--unit M -d | tail -1 | awk -v col="10" '{print $col}')
disk_available=$(df -h --output=avail / | tail -1 | xargs)

timestamp=$(date +"%Y-%m-%d %H:%M:%S")

# Subquery to find matching id in host_info table
host_id="(SELECT id FROM host_info WHERE hostname='$hostname')";

# PSQL command: Inserts server usage data into host_usage table
# Note: be careful with double and single quotes
insert_statement="INSERT INTO host_usage (timestamp, host_id, memory_free, cpu_idle, cpu_kernel, disk_io, disk_available)
VALUES ('$timestamp', $host_id, $memory_free, $cpu_idle, $cpu_kernel, $disk_io, $disk_available');"

# execute the INSERT statement through the psql CLI tool
export PGPASSWORD=$psql_password
psql -h "$psql_host" -p "$psql_port" -U "$psql_user" -d "$db_name" -c "$insert_statement"
