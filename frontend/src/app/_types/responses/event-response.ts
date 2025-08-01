export interface EventResponse {
  contentId: number;
  title: string;
  createdTime: string;
  modifiedTime: string;
  addr1: string;
  addr2: string;
  area: string;
  firstImage: string;
  firstImage2: string;
  mapX: number;
  mapY: number;
  zipCode: string;
  homepage: string;
  overview: string;
  eventStartDate: string;
  eventEndDate: string;
  playTime: string;
  useTimeFestival: string;
  sponsor1: string;
  sponsor1Tel: string;
  sponsor2: string;
  sponsor2Tel: string;
  dbUpdatedAt: string;
}

export interface ErrorResponse { 
  message: string;
};
