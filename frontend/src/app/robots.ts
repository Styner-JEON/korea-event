import type { MetadataRoute } from 'next'
 
export default function robots(): MetadataRoute.Robots {
  const baseUrl = process.env.NEXT_PUBLIC_SITE_URL;
  if (!baseUrl) {
    throw new Error('NEXT_PUBLIC_SITE_URL is not set');
  }

  return {
    rules: {
      userAgent: '*',
      allow: '/',
      disallow: ['/api/', '/signup', '/login', '/events/favorites'],
    },
    sitemap: `${baseUrl}/sitemap.xml`,
  };
}