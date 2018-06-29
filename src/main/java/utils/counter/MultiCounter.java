package utils.counter;

public class MultiCounter {
	private int max;
	private int min = 0;
	private int counter = 0;
	private MultiCounter nextCounter;
	private MultiCounterOnZero multiCounterOnZero;
	private MultiCounterOnOverflow multiCounterOnOverflow;

	public MultiCounter(int min, int max, int initialValue, MultiCounter nextCounter, MultiCounterOnZero multiCounterOnZero, MultiCounterOnOverflow multiCounterOnOverflow) {
		this.max = max;
		this.min = min;
		this.counter = initialValue;
		this.nextCounter = nextCounter;
		this.multiCounterOnZero = multiCounterOnZero;
		this.multiCounterOnOverflow = multiCounterOnOverflow;
	}

	public MultiCounter(MultiCounter nextCounter, MultiCounterOnZero multiCounterOnZero, MultiCounterOnOverflow multiCounterOnOverflow) {
		this.nextCounter = nextCounter;
		this.multiCounterOnZero = multiCounterOnZero;
		this.multiCounterOnOverflow = multiCounterOnOverflow;
	}

	public synchronized void inc() {
		setCounter(getCounter() + 1);
		if (getCounter() >= getMax()) {
			setCounter(getMin());
			if (getMultiCounterOnOverflow() != null) {
				getMultiCounterOnOverflow().execute();
			}
			if (getNextCounter() != null) {
				getNextCounter().inc();
			}
		}
	}

	public synchronized void dec() {
		setCounter(getCounter() - 1);
		if (getCounter() < getMin()) {
			setCounter(getMax() - 1);
			if (getMultiCounterOnZero() != null) {
				getMultiCounterOnZero().execute();
			}
			if (getNextCounter() != null) {
				getNextCounter().dec();
			}
		}
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public MultiCounter getNextCounter() {
		return nextCounter;
	}

	public void setNextCounter(MultiCounter nextCounter) {
		this.nextCounter = nextCounter;
	}

	public MultiCounterOnZero getMultiCounterOnZero() {
		return multiCounterOnZero;
	}

	public void setMultiCounterOnZero(MultiCounterOnZero multiCounterOnZero) {
		this.multiCounterOnZero = multiCounterOnZero;
	}

	public MultiCounterOnOverflow getMultiCounterOnOverflow() {
		return multiCounterOnOverflow;
	}

	public void setMultiCounterOnOverflow(MultiCounterOnOverflow multiCounterOnOverflow) {
		this.multiCounterOnOverflow = multiCounterOnOverflow;
	}
}
