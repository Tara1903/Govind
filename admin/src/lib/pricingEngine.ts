export type DiscountType = 'percentage' | 'fixed' | 'none';

export interface BulkTier {
  min_qty: number;
  type: 'percentage' | 'fixed';
  value: number;
  status: 'active' | 'inactive';
}

export interface PricingResult {
  baseUnitPrice: number;
  effectiveUnitPrice: number;
  appliedTier: BulkTier | null;
  discountType: DiscountType;
  discountValue: number;
  discountAmountPerUnit: number;
  totalDiscount: number;
  subtotal: number;
  nextTier: BulkTier | null;
  remainingQuantity: number | null;
}

export function calculateProductPrice(
  basePrice: number,
  quantity: number,
  wholesalePricing: any, // The JSON from bundle_items.wholesale_pricing
  experience: string
): PricingResult {
  let effectiveUnitPrice = basePrice;
  let appliedTier: BulkTier | null = null;
  let discountType: DiscountType = 'none';
  let discountValue = 0;
  let discountAmountPerUnit = 0;

  let activeTiers: BulkTier[] = [];
  
  // Only apply wholesale pricing if eligible, and if not in Kitchen experience (unless specified otherwise)
  if (wholesalePricing && wholesalePricing.wholesale_eligible && experience !== 'KITCHEN') {
    activeTiers = (wholesalePricing.tiers || [])
      .filter((t: BulkTier) => t.status === 'active')
      .sort((a: BulkTier, b: BulkTier) => a.min_qty - b.min_qty);

    // Find highest applicable tier
    for (const tier of activeTiers) {
      if (quantity >= tier.min_qty) {
        appliedTier = tier;
      }
    }

    if (appliedTier) {
      discountType = appliedTier.type;
      discountValue = appliedTier.value;
      
      if (appliedTier.type === 'percentage') {
        // Round to 2 decimal places
        discountAmountPerUnit = Number((basePrice * (appliedTier.value / 100)).toFixed(2));
        effectiveUnitPrice = Number((basePrice - discountAmountPerUnit).toFixed(2));
      } else if (appliedTier.type === 'fixed') {
        effectiveUnitPrice = Number(appliedTier.value.toFixed(2));
        discountAmountPerUnit = Number((basePrice - effectiveUnitPrice).toFixed(2));
      }
    }
  }

  // Find next tier
  let nextTier: BulkTier | null = null;
  let remainingQuantity: number | null = null;
  
  if (activeTiers.length > 0) {
    for (const tier of activeTiers) {
      if (tier.min_qty > quantity) {
        nextTier = tier;
        remainingQuantity = tier.min_qty - quantity;
        break; // Tiers are sorted, so the first one > quantity is the next tier
      }
    }
  }

  const subtotal = Number((effectiveUnitPrice * quantity).toFixed(2));
  const totalDiscount = Number((discountAmountPerUnit * quantity).toFixed(2));

  return {
    baseUnitPrice: basePrice,
    effectiveUnitPrice,
    appliedTier,
    discountType,
    discountValue,
    discountAmountPerUnit,
    totalDiscount,
    subtotal,
    nextTier,
    remainingQuantity
  };
}
