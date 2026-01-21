import CallToAction from "../src/components/Home/CallToAction";
import Features from "../src/components/Home/Features";
import Hero from "../src/components/Home/Hero";
import HowItWorks from "../src/components/Home/HowItWorks";

export default function Home() {

    return (
        <>
            <Hero/>
            <HowItWorks />
            <Features />
            <CallToAction />
        </>
    );
}