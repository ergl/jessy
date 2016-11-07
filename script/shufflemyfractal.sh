#!/usr/bin/env bash

# This script shuffles the id of ips in my fractal file.
#
# Therefore, in case of groups with more than one member,
# the processes are selected from different sites.

if [[ -f myfractal.tmp ]]; then
  rm -f myfractal.tmp
fi

group_size=$(grep "group_size =" config.property | awk '{print $3}')
node_count=$(grep "<node " myfractal.xml | wc -l | awk '{print $1}')

id=0
min=-1
while read line; do
  if [[ ${line} == *node*id* ]]; then
    if [[ ${id} -eq ${min} ]]; then
      id=$(($id+1))
    fi

    replace=$(echo ${line} | awk '{print $2}')
    newline=$(echo ${line} | sed "s/$replace/id=\"$id\"/g")
    echo ${newline} >> myfractal.tmp

    if [[ ${id} -eq ${min}+1 ]]; then
      min=$(($min+1))
    fi

    id=$(($id+$group_size))
    id=$(($id%$node_count))
  fi
done < myfractal.xml

sort myfractal.tmp -o myfractal.tmp
rm -f myfractal.xml

echo '<?xml version="1.0" encoding="ISO-8859-1" ?>' >> myfractal.xml
echo '<FRACTAL>' >> myfractal.xml
echo '<BootstrapIdentity>' >> myfractal.xml
echo '<nodelist>' >> myfractal.xml

cat myfractal.tmp >> myfractal.xml

echo '</nodelist>' >> myfractal.xml
echo '</BootstrapIdentity>' >> myfractal.xml
echo '</FRACTAL>' >> myfractal.xml

rm -f myfractal.tmp
