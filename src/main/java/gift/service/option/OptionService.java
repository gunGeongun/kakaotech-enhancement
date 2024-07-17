package gift.service.option;

import gift.model.gift.Gift;
import gift.model.option.*;
import gift.repository.gift.GiftRepository;
import gift.repository.option.OptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class OptionService {

    private OptionRepository optionRepository;
    private GiftRepository giftRepository;

    @Autowired
    public OptionService(OptionRepository optionRepository, GiftRepository giftRepository) {
        this.optionRepository = optionRepository;
        this.giftRepository = giftRepository;
    }

    @Transactional
    public void addOptionToGift(Long giftId, OptionRequest optionRequest) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다 id :  " + giftId));
        Option option = optionRequest.toEntity();

        checkDuplicateOptionName(gift, option.getName());

        gift.addOption(option);
        giftRepository.save(gift);
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getAllOptions() {
        List<OptionResponse> options = optionRepository.findAll().stream()
                .map(OptionResponse::from)
                .toList();
        return options;
    }

    @Transactional(readOnly = true)
    public List<OptionResponse> getOptionsByGiftId(Long giftId) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다 id :  " + giftId));

        return gift.getOptions().stream()
                .map(OptionResponse::from)
                .toList();
    }

    @Transactional
    public void updateOptionToGift(Long giftId, Long optionId, OptionRequest optionRequest) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다 id :  " + giftId));
        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("해당 옵션을 찾을 수 없습니다 id :  " + optionId));

        checkDuplicateOptionName(gift, option.getName());

        option.modify(optionRequest.getName(), optionRequest.getQuantity());

    }

    @Transactional
    public void deleteOptionFromGift(Long giftId, Long optionId) {
        Gift gift = giftRepository.findById(giftId)
                .orElseThrow(() -> new NoSuchElementException("해당 상품을 찾을 수 없습니다 id :  " + giftId));
        Option option = optionRepository.findById(optionId)
                .orElseThrow(() -> new NoSuchElementException("해당 옵션을 찾을 수 없습니다 id :  " + optionId));

        gift.removeOption(option);
        giftRepository.save(gift);
        optionRepository.delete(option);
    }


    public void checkDuplicateOptionName(Gift gift, String optionName) {
        List<Option> options = gift.getOptions();

        long distinctCount = options.stream()
                .map(Option::getName)
                .distinct()
                .count();

        if (distinctCount != options.size()) {
            throw new IllegalArgumentException("중복된 옵션 이름이 있습니다!");
        }

    }
}