package com.jangburich.domain.order.application;

import com.amazonaws.services.kms.model.NotFoundException;
import com.jangburich.domain.common.Status;
import com.jangburich.domain.menu.domain.Menu;
import com.jangburich.domain.menu.domain.repository.MenuRepository;
import com.jangburich.domain.order.domain.Cart;
import com.jangburich.domain.order.domain.Orders;
import com.jangburich.domain.order.domain.repository.CartRepository;
import com.jangburich.domain.order.domain.repository.OrdersRepository;
import com.jangburich.domain.order.dto.request.AddCartRequest;
import com.jangburich.domain.order.dto.request.OrderRequest;
import com.jangburich.domain.order.dto.response.CartResponse;
import com.jangburich.domain.order.dto.response.GetCartItemsResponse;
import com.jangburich.domain.store.domain.Store;
import com.jangburich.domain.store.domain.repository.StoreRepository;
import com.jangburich.domain.store.domain.repository.StoreTeamRepository;
import com.jangburich.domain.team.domain.Team;
import com.jangburich.domain.team.domain.repository.TeamRepository;
import com.jangburich.domain.user.domain.User;
import com.jangburich.domain.user.repository.UserRepository;
import com.jangburich.global.payload.Message;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final OrdersRepository ordersRepository;
    private final TeamRepository teamRepository;
    private final StoreTeamRepository storeTeamRepository;

    @Transactional
    public Message addCart(String userProviderId, AddCartRequest addCartRequest) {
        User user = userRepository.findByProviderId(userProviderId)
                .orElseThrow(() -> new NullPointerException());

        Menu menu = menuRepository.findById(addCartRequest.menuId())
                .orElseThrow(() -> new IllegalArgumentException("등록된 메뉴를 찾을 수 없습니다. "));

        System.out.println("menu.getId() = " + menu.getId());
        System.out.println("user.getUserId() = " + user.getUserId());

        Optional<Cart> optionalCart = cartRepository.findByUserIdAndMenuId(user.getUserId(), menu.getId());

        Store store = storeRepository.findById(addCartRequest.storeId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 가게 id 입니다."));

        if (optionalCart.isPresent()) {
            Cart existingCart = optionalCart.get();
            existingCart.updateQuantity(existingCart.getQuantity() + addCartRequest.quantity());

            return Message.builder()
                    .message("장바구니에 상품을 추가했습니다.")
                    .build();
        }

        Cart newCart = Cart.builder()
                .quantity(addCartRequest.quantity())
                .menu(menu)
                .user(user)
                .store(store)
                .orders(null)
                .build();

        cartRepository.save(newCart);


        return Message.builder()
                .message("장바구니에 상품을 추가했습니다.")
                .build();
    }

    public CartResponse getCartItems(String userProviderId) {
        User user = userRepository.findByProviderId(userProviderId)
                .orElseThrow(() -> new NullPointerException());

        List<Cart> carts = cartRepository.findAllByUserAndStatus(user, Status.ACTIVE);

        if (carts.isEmpty()) {
            return CartResponse.of(List.of(), 0);
        }

        List<GetCartItemsResponse> cartItems = carts.stream()
                .map(cart -> GetCartItemsResponse.of(
                        cart.getMenu().getName(),
                        cart.getMenu().getDescription(),
                        cart.getQuantity(),
                        cart.getMenu().getPrice()
                ))
                .toList();

        int discountAmount = 0;
        CartResponse cartResponse = CartResponse.of(cartItems, discountAmount);



        return cartResponse;
    }

    @Transactional
    public Message order(String userProviderId, OrderRequest orderRequest) {
        User user = userRepository.findByProviderId(userProviderId)
                .orElseThrow(() -> new NullPointerException());

        Store store = storeRepository.findById(orderRequest.storeId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 가게 id 입니다."));

        Team team = teamRepository.findById(orderRequest.teamId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 그룹 id 입니다."));

        List<Cart> existingCarts = cartRepository.findAllByUserAndStore(user, store);

        List<Cart> mergedCarts = mergeCarts(existingCarts, orderRequest.items(), user, store);


        Orders orders = saveOrder(user, store, team, orderRequest);

        associateCartsWithOrder(mergedCarts, orders);

        cartRepository.saveAll(mergedCarts);

        return Message.builder()
                .message("주문이 완료되었습니다.")
                .build();
    }

    private List<Cart> mergeCarts(List<Cart> existingCarts, List<OrderRequest.OrderItemRequest> items, User user, Store store) {
        for (OrderRequest.OrderItemRequest item : items) {
            Optional<Cart> existingCart = findCartByMenuId(existingCarts, item.menuId());
            if (existingCart.isPresent()) {
                existingCart.get().updateQuantity(existingCart.get().getQuantity() + item.quantity());
                existingCart.get().updateStatus(Status.INACTIVE);
                continue;
            }
            Cart newCart = createNewCart(item, user, store);
            existingCarts.add(newCart);
        }
        return existingCarts;
    }

    private Optional<Cart> findCartByMenuId(List<Cart> carts, Long menuId) {
        return carts.stream()
                .filter(cart -> cart.getMenu().getId().equals(menuId))
                .findFirst();
    }

    private Cart createNewCart(OrderRequest.OrderItemRequest item, User user, Store store) {
        Menu menu = menuRepository.findById(item.menuId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 메뉴 ID입니다."));
        return Cart.builder()
                .quantity(item.quantity())
                .menu(menu)
                .user(user)
                .store(store)
                .orders(null)
                .build();
    }

    private Orders saveOrder(User user, Store store, Team team, OrderRequest orderRequest) {
        Orders orders = Orders.builder()
                .store(store)
                .user(user)
                .team(team)
                .build();
        return ordersRepository.save(orders);
    }

    private void associateCartsWithOrder(List<Cart> carts, Orders orders) {
        carts.forEach(cart -> cart.updateOrders(orders));
    }

    public Message useMealTicket(String userProviderId, Long orderId) {
        User user = userRepository.findByProviderId(userProviderId)
                .orElseThrow(() -> new NullPointerException());

        Orders orders = ordersRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("식권을 찾을 수 없습니다"));

        orders.validateUser(user);

        orders.updateStatus(Status.INACTIVE);

        return Message.builder()
                .message("식권을 사용했습니다.")
                .build();
    }
}
