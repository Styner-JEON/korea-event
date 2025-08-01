'use client';

import {
  Pagination,
  PaginationContent,  
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from "@/ui/pagination"
import { usePathname, useSearchParams } from "next/navigation";
import { generatePagination } from "../../_libs/util";

export default function MainPagination({ totalPages }: { totalPages: number }) {
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const currentPage = Number(searchParams.get('page')) || 0;

  // pageNumber로 URL을 생성
  const createPageURL = (pageNumber: number) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', pageNumber.toString());
    return `${pathname}?${params.toString()}`;
  };

  const { allPages, startPage, endPage } = generatePagination(currentPage, totalPages);

  const isPrevDisabled = startPage === 0;
  const isNextDisabled = endPage === totalPages - 1;

  return (
    <Pagination>
      <PaginationContent>
        <PaginationItem>
          {!isPrevDisabled && (
            <PaginationPrevious
              href={createPageURL(startPage - 1)}
            />
          )}
        </PaginationItem>

        {allPages.map((page) => (
          <PaginationItem key={page}>
            <PaginationLink
              href={createPageURL(page)}
              isActive={page === currentPage}
            >
              {page + 1}
            </PaginationLink>
          </PaginationItem>
        ))}

        <PaginationItem>
          {!isNextDisabled && (
            <PaginationNext
              href={createPageURL(endPage + 1)}
            />
          )}
        </PaginationItem>
      </PaginationContent>
    </Pagination>
  );
}