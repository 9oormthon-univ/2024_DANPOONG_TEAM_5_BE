package com.jangburich.domain.store.dto.response;

import org.springframework.data.domain.Page;

import com.jangburich.domain.payment.domain.TeamChargeHistoryResponse;
import com.jangburich.domain.user.domain.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PaymentGroupDetailResponse {
	private String teamName;
	private Integer point;
	private Integer remainPoint;
	private String teamLeaderName;
	private String teamLeaderPhoneNum;
	// TODO 이거 아니고, order 결제 내역으로 변경해야함.
	private Page<TeamChargeHistoryResponse> historyResponses;

	public static PaymentGroupDetailResponse create(String teamName, Integer point, Integer remainPoint,
		User teamLeader, Page<TeamChargeHistoryResponse> historyResponses) {
		PaymentGroupDetailResponse paymentGroupDetailResponse = new PaymentGroupDetailResponse();
		paymentGroupDetailResponse.setTeamName(teamName);
		paymentGroupDetailResponse.setPoint(point);
		paymentGroupDetailResponse.setRemainPoint(remainPoint);
		paymentGroupDetailResponse.setTeamLeaderName(teamLeader.getNickname());
		paymentGroupDetailResponse.setTeamLeaderPhoneNum(teamLeader.getPhoneNumber());
		paymentGroupDetailResponse.setHistoryResponses(historyResponses);
		return paymentGroupDetailResponse;
	}
}