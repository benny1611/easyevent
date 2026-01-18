import CallToAction from "./CallToAction";
import Features from "./Features";
import Hero from "./Hero";
import HowItWorks from "./HowItWorks";

export default function Home() {

    return (
        <>
            <Hero sx={{ flexGrow: 1 }} />
            <HowItWorks />
            <Features />
            <CallToAction />
        </>
    );
}