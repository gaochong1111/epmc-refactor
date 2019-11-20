INPUT_PRISM=qmc-loop/qmc-loop.prism
INPUT_PROPS=qmc-loop/qmc-loop.props # qctl
INPUT_PROPS=qmc-loop/qmc-loop-ltl.props # qltl

INPUT_PRISM=qmc-recursive/qmc-recursive.prism
INPUT_PROPS=qmc-recursive/qmc-recursive-ltl.props

#INPUT_PRISM=qmc-example-6/qmc-example-6.prism
#INPUT_PROPS=qmc-example-6/qmc-example-6.props

# run qctl example
#echo "java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM --property-input-files $INPUT_PROPS --model-input-type prism-qmc"
#java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM --property-input-files $INPUT_PROPS --model-input-type prism-qmc

# run qltl example
echo "java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM  --property-input-files $INPUT_PROPS --model-input-type prism-qmc"
java -jar epmc-qmc.jar check --model-input-files $INPUT_PRISM  --property-input-files $INPUT_PROPS --model-input-type prism-qmc
