package com.jangburich.domain.store.service;

import com.jangburich.domain.store.dto.StoreTeamResponse;
import com.jangburich.domain.store.dto.response.StoreSearchDetailsResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.jangburich.domain.menu.domain.Menu;
import com.jangburich.domain.menu.domain.MenuCreateRequestDTO;
import com.jangburich.domain.menu.repository.MenuRepository;
import com.jangburich.domain.order.domain.Cart;
import com.jangburich.domain.order.domain.OrderResponse;
import com.jangburich.domain.order.domain.OrderStatus;
import com.jangburich.domain.order.domain.Orders;
import com.jangburich.domain.order.domain.repository.CartRepository;
import com.jangburich.domain.order.domain.repository.OrdersRepository;
import com.jangburich.domain.owner.domain.Owner;
import com.jangburich.domain.owner.domain.repository.OwnerRepository;
import com.jangburich.domain.payment.domain.repository.TeamChargeHistoryRepository;
import com.jangburich.domain.point.domain.repository.PointTransactionRepository;
import com.jangburich.domain.store.domain.Category;
import com.jangburich.domain.store.domain.Store;
import com.jangburich.domain.store.domain.StoreAdditionalInfoCreateRequestDTO;
import com.jangburich.domain.store.domain.StoreChargeHistoryResponse;
import com.jangburich.domain.store.domain.StoreCreateRequestDTO;
import com.jangburich.domain.store.domain.StoreGetResponseDTO;
import com.jangburich.domain.store.domain.StoreTeam;
import com.jangburich.domain.store.domain.StoreUpdateRequestDTO;
import com.jangburich.domain.store.dto.StoreTeamResponse;
import com.jangburich.domain.store.dto.condition.StoreSearchCondition;
import com.jangburich.domain.store.dto.condition.StoreSearchConditionWithType;
import com.jangburich.domain.store.dto.response.OrdersDetailResponse;
import com.jangburich.domain.store.dto.response.OrdersGetResponse;
import com.jangburich.domain.store.dto.response.OrdersTodayResponse;
import com.jangburich.domain.store.dto.response.PaymentGroupDetailResponse;
import com.jangburich.domain.store.dto.response.SearchStoresResponse;
import com.jangburich.domain.store.dto.response.StoreSearchDetailsResponse;
import com.jangburich.domain.store.exception.OrdersNotFoundException;
import com.jangburich.domain.store.repository.StoreRepository;
import com.jangburich.domain.store.repository.StoreTeamRepository;
import com.jangburich.domain.team.domain.Team;
import com.jangburich.domain.team.domain.repository.TeamRepository;
import com.jangburich.domain.user.domain.User;
import com.jangburich.domain.user.repository.UserRepository;
import com.jangburich.global.config.s3.S3Service;
import com.jangburich.global.error.DefaultNullPointerException;
import com.jangburich.global.payload.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class StoreService {

	private final StoreRepository storeRepository;
	private final OwnerRepository ownerRepository;
	private final UserRepository userRepository;
	private final StoreTeamRepository storeTeamRepository;
	private final TeamRepository teamRepository;
	private final TeamChargeHistoryRepository teamChargeHistoryRepository;
	private final MenuRepository menuRepository;
	private final S3Service s3Service;
	private final OrdersRepository ordersRepository;
	private final CartRepository cartRepository;
	private final PointTransactionRepository pointTransactionRepository;

	@Transactional
	public void createStore(String authentication, StoreCreateRequestDTO storeCreateRequestDTO, MultipartFile image,
		List<MultipartFile> menuImages) {

		User user = userRepository.findByProviderId(authentication)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.save(Store.of(owner, storeCreateRequestDTO));

		store.setRepresentativeImage(s3Service.uploadImageToS3(image));

		owner.setBusinessName(storeCreateRequestDTO.getBusinessName());
		owner.setPhoneNumber(storeCreateRequestDTO.getPhoneNumber());
		owner.setBusinessRegistrationNumber(storeCreateRequestDTO.getBusinessRegistrationNumber());
		owner.setOpeningDate(storeCreateRequestDTO.getOpeningDate());
		owner.setName(storeCreateRequestDTO.getBusinessName());
		user.setName(storeCreateRequestDTO.getBusinessName());
		user.setAgreeMarketing(storeCreateRequestDTO.getAgreeMarketing());
		user.setAgreeAdvertisement(storeCreateRequestDTO.getAgreeAdvertise());
		user.setPhoneNumber(storeCreateRequestDTO.getPhoneNumber());

		if (menuImages != null && storeCreateRequestDTO.getMenuCreateRequestDTOS() != null) {
			List<MenuCreateRequestDTO> menus = storeCreateRequestDTO.getMenuCreateRequestDTOS();
			for (int i = 0; i < menus.size(); i++) {
				MultipartFile menuImage = menuImages.size() > i ? menuImages.get(i) : null;
				if (menuImage != null) {
					String imageUrl = s3Service.uploadImageToS3(menuImage);
					menuRepository.save(Menu.create(menus.get(i).getName(), menus.get(i).getDescription(), imageUrl,
						menus.get(i).getPrice(), store));
				}
			}
		}

	}

	@Transactional
	public void createAdditionalInfo(String authentication,
		StoreAdditionalInfoCreateRequestDTO storeAdditionalInfoCreateRequestDTO) {
		User user = userRepository.findByProviderId(authentication)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		store.setReservationAvailable(storeAdditionalInfoCreateRequestDTO.getReservationAvailable());
		store.setMinPrepayment(storeAdditionalInfoCreateRequestDTO.getMinPrepayment());
		store.setMaxReservation(storeAdditionalInfoCreateRequestDTO.getMaxReservation());
		store.setPrepaymentDuration(storeAdditionalInfoCreateRequestDTO.getPrepaymentDuration());

		storeRepository.save(store);
	}

	@Transactional
	public void updateStore(String userId, StoreUpdateRequestDTO storeUpdateRequestDTO) {
		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		if (!store.getOwner().getUser().getProviderId().equals(userId)) {
			throw new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION);
		}

		storeRepository.save(updateStore(store, storeUpdateRequestDTO));
	}

	@Transactional
	public Store updateStore(Store store, StoreUpdateRequestDTO storeUpdateRequestDTO) {
		if (storeUpdateRequestDTO.getCategory() != null) {
			store.setCategory(storeUpdateRequestDTO.getCategory());
		}
		if (storeUpdateRequestDTO.getReservationAvailable() != null) {
			store.setReservationAvailable(storeUpdateRequestDTO.getReservationAvailable());
		}
		if (storeUpdateRequestDTO.getRepresentativeImage() != null) {
			store.setRepresentativeImage(storeUpdateRequestDTO.getRepresentativeImage());
		}
		if (storeUpdateRequestDTO.getMaxReservation() != null) {
			store.setMaxReservation(storeUpdateRequestDTO.getMaxReservation());
		}
		if (storeUpdateRequestDTO.getMinPrepayment() != null) {
			store.setMinPrepayment(storeUpdateRequestDTO.getMinPrepayment());
		}
		if (storeUpdateRequestDTO.getPrepaymentDuration() != null) {
			store.setPrepaymentDuration(storeUpdateRequestDTO.getPrepaymentDuration());
		}
		if (storeUpdateRequestDTO.getIntroduction() != null) {
			store.setIntroduction(storeUpdateRequestDTO.getIntroduction());
		}
		if (storeUpdateRequestDTO.getLatitude() != null) {
			store.setLatitude(storeUpdateRequestDTO.getLatitude());
		}
		if (storeUpdateRequestDTO.getLongitude() != null) {
			store.setLongitude(storeUpdateRequestDTO.getLongitude());
		}
		if (storeUpdateRequestDTO.getAddress() != null) {
			store.setAddress(storeUpdateRequestDTO.getAddress());
		}
		if (storeUpdateRequestDTO.getLocation() != null) {
			store.setLocation(storeUpdateRequestDTO.getLocation());
		}
		if (storeUpdateRequestDTO.getDayOfWeek() != null) {
			store.setWorkDays(storeUpdateRequestDTO.getDayOfWeek());
		}
		if (storeUpdateRequestDTO.getOpenTime() != null) {
			store.setOpenTime(storeUpdateRequestDTO.getOpenTime());
		}
		if (storeUpdateRequestDTO.getCloseTime() != null) {
			store.setCloseTime(storeUpdateRequestDTO.getCloseTime());
		}
		return store;
	}

	public StoreGetResponseDTO getStoreInfo(String authentication) {
		User user = userRepository.findByProviderId(authentication)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_PARAMETER));

		if (!store.getOwner().getUser().getProviderId().equals(authentication)) {
			throw new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION);
		}

		return new StoreGetResponseDTO().of(store);
	}

	public List<com.jangburich.domain.store.dto.StoreTeamResponse> getPaymentGroup(String userId) {
		User user = userRepository.findByProviderId(userId)
				.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
				.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
				.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		return storeTeamRepository.findAllByStore(store)
			.stream()
			.map(storeTeamResponseDTO -> new StoreTeamResponse(storeTeamResponseDTO.id(),
				storeTeamResponseDTO.remainPoint(),
				storeTeamResponseDTO.teamId(), storeTeamResponseDTO.teamName(), storeTeamResponseDTO.teamDescription(),
				storeTeamResponseDTO.storeId(), storeTeamResponseDTO.updatedAt(), store.getMaxReservation()))
			.toList();
	}

	public Page<SearchStoresResponse> searchByCategory(final String authentication, final Integer searchRadius,
		final Category category, Double lat, Double lon, final Pageable pageable) {
		User user = userRepository.findByProviderId(authentication)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));
		return storeRepository.findStoresByCategory(user.getUserId(), searchRadius, category, lat, lon,
			pageable);
	}

	public Page<SearchStoresResponse> searchStores(final String authentication, final String keyword,
		final Pageable pageable) {
		User user = userRepository.findByProviderId(authentication)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));
		return storeRepository.findStores(user.getUserId(), keyword, pageable);
	}

	public PaymentGroupDetailResponse getPaymentGroupDetail(String userId, Long teamId) {
		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Team team = teamRepository.findById(teamId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_PARAMETER));

		User teamLeader = userRepository.findById(team.getTeamLeader().getUser_id())
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_PARAMETER));

		StoreTeam storeTeam = storeTeamRepository.findByStoreIdAndTeamId(store.getId(), team.getId())
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_PARAMETER));

		List<Orders> orders = ordersRepository.findAllByTeam(team);

		List<OrderResponse> orderResponse = orders.stream().map(order -> {
			int price = 0;
			for (Cart cart : cartRepository.findAllByOrders(order)) {
				price += cart.getMenu().getPrice();
			}
			return new OrderResponse(order.getUser().getName(), order.getUpdatedAt(), String.valueOf(price));
		}).toList();

		return PaymentGroupDetailResponse.create(team.getName(), storeTeam.getPoint(), storeTeam.getRemainPoint(),
			teamLeader, orderResponse);
	}

	public List<StoreChargeHistoryResponse> getPaymentHistory(String userId) {
		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		return pointTransactionRepository.findAllByStore(store);
	}

	public List<OrdersGetResponse> getOrdersLast(String userId) {
		List<OrdersGetResponse> ordersGetResponses = new ArrayList<>();

		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		LocalDateTime todayStart = LocalDate.now().atStartOfDay();
		List<Orders> allByStore = ordersRepository.findOrdersByStoreAndDateAndStatusNative(store.getId(), todayStart,
			"TICKET_USED");

		for (Orders orders : allByStore) {
			OrdersGetResponse newOrdersGetResponse = new OrdersGetResponse();
			List<Cart> carts = cartRepository.findAllByOrders(orders);
			newOrdersGetResponse.setId(orders.getId());
			newOrdersGetResponse.setMenuNames(carts.size() == 1 ? carts.get(0).getMenu().getName() :
				carts.get(0).getMenu().getName() + " 외 " + (carts.size() - 1) + "개");
			newOrdersGetResponse.setCount(carts.size());
			newOrdersGetResponse.setDate(orders.getUpdatedAt());
			int price = 0;
			for (Cart cart : carts) {
				price += (cart.getMenu().getPrice() * cart.getQuantity());
			}
			newOrdersGetResponse.setPrice(price);
			ordersGetResponses.add(newOrdersGetResponse);
		}

		return ordersGetResponses;
	}

	public OrdersTodayResponse getTodayOrders(String userId) {
		List<OrdersGetResponse> ordersGetResponses = new ArrayList<>();

		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Owner owner = ownerRepository.findByUser(user)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Store store = storeRepository.findByOwner(owner)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		LocalDateTime startOfDay = LocalDate.now().atStartOfDay(); // 오늘 시작
		LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay(); // 내일 시작 (오늘의 끝)

		List<Orders> allByStore = ordersRepository.findOrdersByStoreAndTodayDateAndStatus(store.getId(), startOfDay,
			endOfDay, OrderStatus.TICKET_USED);
		int totalPrice = 0;
		for (Orders orders : allByStore) {
			OrdersGetResponse newOrdersGetResponse = new OrdersGetResponse();
			List<Cart> carts = cartRepository.findAllByOrders(orders);

			newOrdersGetResponse.setId(orders.getId());
			newOrdersGetResponse.setMenuNames(carts.size() == 1 ? carts.get(0).getMenu().getName() :
				carts.get(0).getMenu().getName() + " 외 " + (carts.size() - 1) + "개");
			newOrdersGetResponse.setCount(carts.size());
			newOrdersGetResponse.setDate(orders.getUpdatedAt());
			int price = 0;
			for (Cart cart : carts) {
				price += (cart.getMenu().getPrice() * cart.getQuantity());
			}
			newOrdersGetResponse.setPrice(price);
			totalPrice += price;
			ordersGetResponses.add(newOrdersGetResponse);
		}

		OrdersTodayResponse ordersTodayResponse = new OrdersTodayResponse();
		ordersTodayResponse.setOrdersGetResponses(ordersGetResponses);
		ordersTodayResponse.setTotalPrice(totalPrice);

		return ordersTodayResponse;
	}

	public OrdersDetailResponse getOrderDetails(String userId, Long orderId) {
		OrdersDetailResponse ordersDetailResponse = new OrdersDetailResponse();

		userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		Orders orders = ordersRepository.findById(orderId).orElseThrow(OrdersNotFoundException::new);

		ordersDetailResponse.setId(orders.getId());
		ordersDetailResponse.setTeamName(orders.getTeam().getName());
		ordersDetailResponse.setTeamUserName(orders.getUser().getName());

		List<Cart> carts = cartRepository.findAllByOrders(orders);
		List<OrdersDetailResponse.Menu> menus = new ArrayList<>();
		Integer amount = 0;
		Integer price = 0;
		for (Cart cart : carts) {
			OrdersDetailResponse.Menu menu = new OrdersDetailResponse.Menu();
			menu.setMenuName(cart.getMenu().getName());
			menu.setAmount(cart.getQuantity());
			menus.add(menu);
			amount += cart.getQuantity();
			price += (cart.getQuantity() * cart.getMenu().getPrice());
		}
		ordersDetailResponse.setMenus(menus);
		ordersDetailResponse.setDateTime(orders.getUpdatedAt());
		ordersDetailResponse.setAmount(amount);
		ordersDetailResponse.setTotalPrice(price);
		ordersDetailResponse.setDiscountPrice(0);

		return ordersDetailResponse;
	}

	public StoreSearchDetailsResponse storeSearchDetails(String userId, Long storeId) {
		User user = userRepository.findByProviderId(userId)
			.orElseThrow(() -> new DefaultNullPointerException(ErrorCode.INVALID_AUTHENTICATION));

		StoreSearchDetailsResponse storeSearchDetails = storeRepository.findStoreSearchDetails(user.getUserId(),
			storeId);

		return storeSearchDetails;
	}
}