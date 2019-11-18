package org.highmed.dsf.fhir.variables;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.highmed.dsf.bpe.Constants;

public class Outputs implements Serializable
{
	private final List<Output> outputs = new ArrayList<>();

	public Outputs()
	{}

	public Outputs(Collection<? extends Output> outputs)
	{
		if (outputs != null)
			this.outputs.addAll(outputs);
	}

	public List<Output> getOutputs()
	{
		return Collections.unmodifiableList(outputs);
	}

	public void add(Output output)
	{
		outputs.add(output);
	}

	public void addErrorOutput(String error) {
		Output output = new Output(Constants.CODESYSTEM_HIGHMED_BPMN, Constants.CODESYSTEM_HIGHMED_BPMN_VALUE_ERROR_MESSAGE, error);
		outputs.add(output);
	}
}
