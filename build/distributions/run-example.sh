# run qctl example
# echo java -jar epmc-qmc.jar check --model-input-files qmc-loop.prism --property-input-files qmc-loop.props --model-input-type prism-qmc
# java -jar epmc-qmc.jar check --model-input-files qmc-loop.prism --property-input-files qmc-loop.props --model-input-type prism-qmc

# run qltl example
echo java -jar epmc-qmc.jar check --model-input-files qmc-loop.prism --property-input-files qmc-ltl.props --model-input-type prism-qmc
java -jar epmc-qmc.jar check --model-input-files qmc-loop.prism --property-input-files qmc-ltl.props --model-input-type prism-qmc
