package leadIq.transaction.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties
public class Transaction {
	private double amount;
	private String type;
	private Long transactionid;
	private Long parentId;
	private List<Long> childrenTransactionIdList;
	private double childerSum;
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Long getTransactionid() {
		return transactionid;
	}
	public void setTransactionid(Long transactionid) {
		this.transactionid = transactionid;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public List<Long> getChildrenTransactionIdList() {
		return childrenTransactionIdList;
	}
	public void setChildrenTransactionIdList(List<Long> childrenTransactionIdList) {
		this.childrenTransactionIdList = childrenTransactionIdList;
	}
	public double getChilderSum() {
		return childerSum;
	}
	public void setChilderSum(double childerSum) {
		this.childerSum = childerSum;
	}
	
}
