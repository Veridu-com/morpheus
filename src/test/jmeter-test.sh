#!/bin/bash

jmeter -n -l samples.csv -j jmeter.log -t jmeter-test-plan.jmx
