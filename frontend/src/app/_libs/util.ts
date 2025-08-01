export const  generatePagination = (currentPage: number, totalPages: number) => {
  const BLOCK_SIZE = Number(process.env.NEXT_PUBLIC_PAGINATION_BLOCK_SIZE);
  const blockNumber = Math.floor(currentPage / BLOCK_SIZE);
  const startPage = blockNumber * BLOCK_SIZE;
  const endPage = Math.min((blockNumber + 1) * BLOCK_SIZE - 1, totalPages - 1); 

  return {
    allPages: Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i),
    startPage,
    endPage,
  };
}
