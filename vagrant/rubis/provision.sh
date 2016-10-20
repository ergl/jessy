#!/usr/bin/env bash

set -e

echo "Provisioning defaults..."
sudo add-apt-repository -y ppa:webupd8team/java > /dev/null 2>&1
sudo apt-get update --fix-missing > /dev/null 2>&1
sudo apt-get install -q -y g++ make git curl vim > /dev/null 2>&1

echo "Setting up jdk..."
sudo apt-get -y upgrade > /dev/null 2>&1
echo debconf shared/accepted-oracle-license-v1-1 select true | sudo debconf-set-selections  > /dev/null 2>&1
echo debconf shared/accepted-oracle-license-v1-1 seen true | sudo debconf-set-selections > /dev/null 2>&1
sudo apt-get -y install oracle-java8-installer > /dev/null 2>&1