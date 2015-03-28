#!/bin/bash

mvn -e -DuniqueVersion=false -DperformRelease=true compile deploy

