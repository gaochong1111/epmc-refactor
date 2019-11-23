#!/bin/bash
function contains() {
    local n=$#
    # local value=${!n}
    local value=${!n}
    for ((i=1;i < $#;i++)) {
        # echo "$value -- $i -- ${!i}"
        if [[ "${!i}" == "${value}" ]]; then
            echo "y"            
            return 0
        fi
    }
    echo "n"
    return 1
}


function check() {
    # $1: M, $2: TEST 
    QCTL_CASES=("LOOP" "SUPERDENSE" "DISTRIBUTION")
    QLTL_CASES=("LOOP" "RECURSIVE" "EXP6")
    if [[ $1 == "QCTL" ]]; then
        if [[ $(contains "${QCTL_CASES[@]}" $2) == "n" ]]; then
            echo "NOT SUPPORT CASE: $2 for $1!"
            exit
        fi
    fi
    if [[ $1 == "QLTL" ]]; then
        if [[ $(contains "${QLTL_CASES[@]}" $2) == "n" ]]; then
            echo "NOT SUPPORT CASE: $2 for $1!"
            exit
        fi
    fi
}


if [[ $1 == "QCTL" ]]; then
    M="QCTL"
else 
    if [[ $1 == "QLTL" ]]; then
        M="QLTL"
    else
        echo "Usage: run-example.sh QCTL [LOOP|SUPERDENSE|DISTRIBUTION]"
        echo "    or run-example.sh QLTL [LOOP|RECURSIVE|EXP6]"
        exit
    fi
fi

#:<<X

TEST=$2

check $M $TEST 

if [[ $TEST == "LOOP" ]]; then
    INPUT_PRISM=qmc-loop/qmc-loop.prism
    if [[ $M == "QCTL" ]]; then
        INPUT_PROPS=qmc-loop/qmc-loop.props # qctl
    else
        INPUT_PROPS=qmc-loop/qmc-loop-ltl.props # qltl
    fi
fi

if [[ $TEST == "SUPERDENSE" ]]; then
    INPUT_PRISM=qmc-superdense-coding/qmc-superdense-coding.prism
    INPUT_PROPS=qmc-superdense-coding/qmc-superdense-coding.props
fi

if [[ $TEST == "DISTRIBUTION" ]]; then
    INPUT_PRISM=qmc-key-distribution/qmc-key-distribution.prism
    INPUT_PROPS=qmc-key-distribution/qmc-key-distribution.props
fi

if [[ $TEST == "RECURSIVE" ]]; then
    INPUT_PRISM=qmc-recursive/qmc-recursive.prism
    INPUT_PROPS=qmc-recursive/qmc-recursive-ltl.props
fi

if [[ $TEST == "EXP6" ]]; then
    INPUT_PRISM=qmc-example-6/qmc-example-6.prism
    INPUT_PROPS=qmc-example-6/qmc-example-6.props
fi


# run qctl example
if [[ $M == "QCTL" ]]; then
    echo "java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM --property-input-files $INPUT_PROPS --model-input-type prism-qmc"
    java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM --property-input-files $INPUT_PROPS --model-input-type prism-qmc
else
# run qltl example
    echo "java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM  --property-input-files $INPUT_PROPS --model-input-type prism-qmc"
    java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM  --property-input-files $INPUT_PROPS --model-input-type prism-qmc
fi
# X
