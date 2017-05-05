function dave_demo
%
% example of calling a DAVE-ML model from within Matlab
%
% requires a copy of DAVEtools (NASA Open Source), available
% from
%
%  http://github/bjax/DAVETools
%
% 2011-03-07 Bruce Jackson, NASA Langley
%            bruce.jackson@nasa.gov
% 2017-05-05 Updated to new URL, email now bjackson@adaptiveaero.com
%
%
pathNeeded = 1;
f = strfind(javaclasspath('-dynamic'),'DAVEtools.jar');
if ~isempty(f)
    if ~isempty(f{1})
        pathNeeded = 0;
    end
end
if pathNeeded
    javaaddpath('/Users/bjax/DAVE/Tools/DAVEtools/DAVEtools.jar')
end
dave = gov.nasa.daveml.dave.DAVE();
dave.setInputFileName('HL20_aero.dml')
ok = dave.parseFile;
if ~ok
    error('unable to parse file')
end

% open model
mass_props = dave.getModel;

% set inputs to zero
inputs = mass_props.getInputVector;
for i=1:inputs.size
    input = inputs.get(i-1);
    input.setValue(0);
end

outputs = mass_props.getOutputVector;

% initialize and cycle the model
mass_props.initialize
mass_props.cycle

% report results

fprintf('Inputs:\n');
theVector = inputs;
printVectorInfo( theVector );

fprintf('Outputs:\n');
theVector = outputs;
printVectorInfo( theVector );

return


function printVectorInfo( theVector )
for i=1:theVector.size
    signal = theVector.get(i-1);
    name  = signal.getName;
    units = signal.getUnits;
    value = signal.getValue;
    fprintf('%34s (%10s): %15.4e\n', char(name), char(units), value)
end
return
