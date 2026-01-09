import axiosInstance from '@/config/axiosConfig';

/**
 * Batch Stock Service with Caching
 * Optimizes stock checking by batching multiple product IDs into single API call
 * and caching results with TTL to minimize redundant requests
 */

// Cache configuration
const CACHE_TTL = 30000; // 30 seconds
const stockCache = new Map(); // Map<productId, {stock, status, timestamp}>

/**
 * Clear all cached stock data
 */
export function clearStockCache() {
    stockCache.clear();
    console.log('🗑️ [STOCK CACHE] Cleared all cached stock data');
}

/**
 * Check if cached data is still valid
 * @param {number} productId
 * @returns {boolean}
 */
function isCacheValid(productId) {
    const cached = stockCache.get(productId);
    if (!cached) return false;

    const age = Date.now() - cached.timestamp;
    return age < CACHE_TTL;
}

/**
 * Get stock info from cache
 * @param {number} productId
 * @returns {Object|null}
 */
function getFromCache(productId) {
    if (isCacheValid(productId)) {
        const cached = stockCache.get(productId);
        console.log(`✅ [CACHE HIT] Product ${productId}:`, cached.stock);
        return cached;
    }
    return null;
}

/**
 * Store stock info in cache
 * @param {number} productId
 * @param {number} stock
 * @param {boolean} status
 */
function storeInCache(productId, stock, status) {
    stockCache.set(productId, {
        stock,
        status,
        timestamp: Date.now()
    });
}

/**
 * Fallback: Get single product stock using existing API
 * @param {number} productId
 * @returns {Promise<Object>}
 */
async function fallbackGetSingleStock(productId) {
    try {
        const [stockResponse, statusResponse] = await Promise.all([
            axiosInstance.get(`/gioHangWeb/maxSoLuong?idCTSP=${productId}`),
            axiosInstance.get(`/banhangweb/trangThaiCTSP?idCTSP=${productId}`)
        ]);

        const stock = stockResponse.data || 0;
        const status = statusResponse.data || false;

        // Cache the result
        storeInCache(productId, stock, status);

        return {
            id: productId,
            stock,
            status
        };
    } catch (error) {
        console.error(`❌ [FALLBACK] Error fetching stock for product ${productId}:`, error);
        // Return default values on error
        return {
            id: productId,
            stock: 0,
            status: false
        };
    }
}

/**
 * Get batch stock information for multiple products
 * Attempts to use batch API first, falls back to parallel individual calls if needed
 * 
 * @param {number[]} productIds - Array of product IDs to check
 * @returns {Promise<Array>} Array of {id, stock, status} objects
 */
export async function getBatchStock(productIds) {
    if (!productIds || productIds.length === 0) {
        return [];
    }

    // Remove duplicates
    const uniqueIds = [...new Set(productIds)];

    // Separate cached and uncached IDs
    const cachedResults = [];
    const uncachedIds = [];

    uniqueIds.forEach(id => {
        const cached = getFromCache(id);
        if (cached) {
            cachedResults.push({
                id,
                stock: cached.stock,
                status: cached.status
            });
        } else {
            uncachedIds.push(id);
        }
    });

    console.log(`📊 [BATCH STOCK] Cache: ${cachedResults.length} hits, ${uncachedIds.length} misses`);

    // If all from cache, return immediately
    if (uncachedIds.length === 0) {
        return cachedResults;
    }

    try {
        // Try batch API endpoint first
        console.log(`🚀 [BATCH API] Fetching stock for ${uncachedIds.length} products`);

        const response = await axiosInstance.post('/gioHangWeb/batchCheckStock', {
            productIds: uncachedIds
        });

        // Cache all results
        response.data.forEach(item => {
            storeInCache(item.id, item.stock, item.status);
        });

        // Combine cached and fresh results
        return [...cachedResults, ...response.data];

    } catch (error) {
        // Batch API failed - check if endpoint exists
        if (error.response?.status === 404) {
            console.warn('⚠️ [BATCH API] Endpoint not found, falling back to parallel calls');
        } else {
            console.warn('⚠️ [BATCH API] Failed, falling back to parallel calls:', error.message);
        }

        // Fallback: Use Promise.all for parallel individual calls
        console.log(`🔄 [FALLBACK] Fetching ${uncachedIds.length} products in parallel`);

        const freshResults = await Promise.all(
            uncachedIds.map(id => fallbackGetSingleStock(id))
        );

        // Combine cached and fresh results
        return [...cachedResults, ...freshResults];
    }
}

/**
 * Get stock status for a single product (with caching)
 * @param {number} productId
 * @returns {Promise<Object>} {stock, status}
 */
export async function getSingleStock(productId) {
    // Check cache first
    const cached = getFromCache(productId);
    if (cached) {
        return {
            stock: cached.stock,
            status: cached.status
        };
    }

    // Use batch function with single ID
    const results = await getBatchStock([productId]);
    return results[0] || { stock: 0, status: false };
}

export const batchStockService = {
    getBatchStock,
    getSingleStock,
    clearStockCache
};
