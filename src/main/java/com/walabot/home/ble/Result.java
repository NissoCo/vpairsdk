package com.example.vpairsdk_flutter.ble;

public class Result<T>
{

	private T         _result;
	private Throwable _throwable;


	public Result(T result)
	{
		_result = result;
	}

	public Result(Throwable throwable)
	{
		_throwable = throwable;
	}

	public T getResult()
	{
		return _result;
	}

	public Throwable getThrowable()
	{
		return _throwable;
	}

	public void setResult(T result)
	{
		_result = result;
	}

	public boolean isSuccessfull()
	{
		return _throwable==null;
	}
}
