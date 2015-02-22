package huck.hestia.db.memory;

import huck.hestia.db.memory.TextDataManager.DataLine;
import huck.hestia.db.memory.TextDataManager.TextDataConverter;


public class TextDataConverterV1 implements TextDataConverter {
	@Override
	public TextDataConverter newInst() {
		return null;
	}

	@Override
	public String getVersion() {
		return null;
	}

	@Override
	public DataLine asset2Line(MemoryAsset asset) {
		return null;
	}

	@Override
	public DataLine shop2Line(MemoryShop asset) {
		return null;
	}

	@Override
	public DataLine debitCode2Line(MemoryDebitCode asset) {
		return null;
	}

	@Override
	public DataLine creditCode2Line(MemoryCreditCode asset) {
		return null;
	}

	@Override
	public DataLine slip2Line(MemorySlip asset) {
		return null;
	}

	@Override
	public DataLine debit2Line(MemoryDebit asset) {
		return null;
	}

	@Override
	public DataLine credit2Line(MemoryCredit asset) {
		return null;
	}

	@Override
	public MemoryAsset line2Asset(DataLine line) {
		return null;
	}

	@Override
	public MemoryShop line2Shop(DataLine line) {
		return null;
	}

	@Override
	public MemoryDebitCode line2DebitCode(DataLine line) {
		return null;
	}

	@Override
	public MemoryCreditCode line2CreditCode(DataLine line) {
		return null;
	}

	@Override
	public MemorySlip line2Slip(DataLine line) {
		return null;
	}

	@Override
	public MemoryDebit line2Debit(DataLine line) {
		return null;
	}

	@Override
	public MemoryCredit line2Credit(DataLine line) {
		return null;
	}

}

