#!/bin/bash

# exit script if one command fails
set -o errexit

# exit script if variable is not set
set -o nounset

samtools faidx $1
awk '{print $1"\tN/A\tN/A\t"$2}' $1.fai > $2

