import MainHeader from "./_ui/main-header";

export default function HomeLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <>            
      <MainHeader />
      {children}
    </>    
  );  
}